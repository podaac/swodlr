package gov.nasa.podaac.swodlr.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import gov.nasa.podaac.swodlr.Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public abstract class AbstractJweCookieStore implements Serializable {
  private static final String COMPRESSION_METHOD = CompressorStreamFactory.LZMA;
  private static final Logger logger = LoggerFactory.getLogger(AbstractJweCookieStore.class);
  private static final CompressorStreamFactory COMPRESSOR_STREAM_FACTORY
      = new CompressorStreamFactory();

  private static SwodlrSecurityProperties securityProperties;

  private final String cookieName;
  protected final Instant creationTime;
  protected final Instant expiration;

  protected AbstractJweCookieStore(String cookieName) {
    this.cookieName = cookieName;
    this.creationTime = Instant.now();
    this.expiration = this.creationTime.plus(getSecurityProperties().sessionLength());
  }

  protected ResponseCookie generateCookie() throws JOSEException {
    JWEObject jwe = generateJwe();
    String value = jwe.serialize();

    if (cookieName.length() + value.length() >= 4096) {
      throw new RuntimeException("Generated cookie too large (>4096)");
    }

    ResponseCookie cookie = ResponseCookie.from(cookieName, value)
        .maxAge(Duration.between(Instant.now(), expiration))
        .path("/")
        // .secure(true) - TODO: Set this based on env
        // .httpOnly(true) - TODO: Set this based on env
        .build();

    return cookie;
  }

  private JWEObject generateJwe() throws JOSEException {
    JWEHeader header = generateHeader();
    Payload payload = new Payload(this.serialize());
    JWEObject jwe = new JWEObject(header, payload);

    jwe.encrypt(getSecurityProperties().encrypter());

    return jwe;
  }

  private byte[] serialize() {
    try {
      ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
      CompressorOutputStream compressorStream = COMPRESSOR_STREAM_FACTORY
          .createCompressorOutputStream(COMPRESSION_METHOD, arrayStream);

      ObjectOutputStream objectStream = new ObjectOutputStream(compressorStream);

      objectStream.writeObject(this);
      objectStream.flush();
      objectStream.close();

      return arrayStream.toByteArray();
    } catch (Exception ex) {
      logger.error("Serialization failed", ex);
      throw new RuntimeException("Session serialization failed");
    }
  }

  private static Optional<AbstractJweCookieStore> deserialize(byte[] buffer) {
    try {
      ByteArrayInputStream arrayStream = new ByteArrayInputStream(buffer);
      CompressorInputStream compressorStream = COMPRESSOR_STREAM_FACTORY
          .createCompressorInputStream(COMPRESSION_METHOD, arrayStream);
      ObjectInputStream objectStream = new ObjectInputStream(compressorStream);

      Object data = objectStream.readObject();
      objectStream.close();

      if (data instanceof AbstractJweCookieStore) {
        return Optional.of((AbstractJweCookieStore) data);
      } else {
        logger.warn("Data not instance of AbstractJweCookieStore");
      }
    } catch (IOException | ClassNotFoundException | CompressorException ex) {
      logger.warn("Deserialization failed", ex);
    }

    return Optional.empty();
  }

  private JWEHeader generateHeader() {
    JWEHeader.Builder builder = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);

    return builder
        .contentType("application/json")
        .customParam("exp", expiration.toString())
        .build();
  }

  protected static <T extends AbstractJweCookieStore> Optional<T> loadCookie(
      Class<T> clazz, HttpCookie cookie
  ) {
    JWEObject jweObject;
    try {
      jweObject = JWEObject.parse(cookie.getValue());
      jweObject.decrypt(getSecurityProperties().decrypter());
    } catch (ParseException | JOSEException ex) {
      logger.warn("Cookie parsing/decryption failed");
      return Optional.empty();
    }

    byte[] data = jweObject.getPayload().toBytes();
    if (data == null) {
      logger.warn("Cookie empty");
      return Optional.empty();
    }

    Optional<AbstractJweCookieStore> unpackedResult = deserialize(data);
    if (!unpackedResult.isPresent()) {
      return Optional.empty();
    }

    AbstractJweCookieStore unpacked = unpackedResult.get();
    if (!clazz.isInstance(unpacked)) {
      logger.warn("Failed to cast to class");
      return Optional.empty();
    }

    T casted = clazz.cast(unpacked);
    if (Instant.now().isAfter(casted.expiration)) {
      logger.warn("Token expired");
      return Optional.empty();
    }

    return Optional.of(casted);
  }

  protected static <T extends AbstractJweCookieStore> Mono<T> loadFromContext(
      Class<T> clazz, String cookieName
  ) {
    return loadExchangeFromContext()
        .flatMap((exchange) -> {
          HttpCookie cookie = exchange.getRequest()
              .getCookies().getFirst(cookieName);

          if (cookie == null) {
            logger.warn("No cookie found: %s".formatted(cookieName));
            return Mono.empty();
          }

          return Mono.justOrEmpty(loadCookie(clazz, cookie));
        });
  }

  protected static <T extends AbstractJweCookieStore> Mono<Void> saveToContext(T obj) {
    return loadExchangeFromContext()
        .flatMap((exchange) -> {
          try {
            exchange
                .getResponse()
                .addCookie(obj.generateCookie());

            return Mono.empty();
          } catch (JOSEException ex) {
            return Mono.error(ex);
          }
        });
  }

  private static Mono<ServerWebExchange> loadExchangeFromContext() {
    return Mono.deferContextual((context) -> {
      if (!context.hasKey(ServerWebExchange.class)) {
        return Mono.error(new RuntimeException("ServerWebExchange not in context"));
      }

      return Mono.just(context.get(ServerWebExchange.class));
    });
  }

  protected static SwodlrSecurityProperties getSecurityProperties() {
    if (securityProperties == null) {
      securityProperties = Utils.applicationContext()
          .getBean(SwodlrSecurityProperties.class);
    }
    return securityProperties;
  }
}
