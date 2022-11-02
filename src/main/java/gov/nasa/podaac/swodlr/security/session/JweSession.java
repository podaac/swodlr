package gov.nasa.podaac.swodlr.security.session;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import gov.nasa.podaac.swodlr.security.SwodlrSecurityProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

public class JweSession implements WebSession, Serializable {
  public static final String SESSION_COOKIE_NAME = "session";

  private static final Logger logger = LoggerFactory.getLogger(JweSession.class);
  private static SwodlrSecurityProperties securityProperties;

  private UUID id;
  private ServerHttpResponse response;
  private final Instant creationTime;
  private final Instant expiration;
  private final Map<String, Object> attributes;

  JweSession(ServerHttpResponse response) {
    this.id = UUID.randomUUID();
    this.response = response;
    this.creationTime = Instant.now();
    this.expiration = this.creationTime.plus(securityProperties.sessionLength());
    this.attributes = new ConcurrentHashMap<>();
  }

  @Override
  public String getId() {
    return id.toString();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public void start() {
    // New session is always sent on save
    return;
  }

  @Override
  public boolean isStarted() {
    return true;
  }

  @Override
  public Mono<Void> changeSessionId() {
    return Mono.defer(() -> {
      id = UUID.randomUUID();
      return Mono.empty();
    });
  }

  @Override
  public Mono<Void> invalidate() {
    // TODO: Invalidate sessions based on creation time
    return Mono.error(new RuntimeException("JweSession::invalidate not implemented"));
  }

  @Override
  public Mono<Void> save() {
    if (this.response == null) {
      return Mono.error(new RuntimeException("response is null"));
    }

    return Mono.defer(() -> {
      ResponseCookie cookie;
      try {
        cookie = generateCookie();
      } catch (JOSEException ex) {
        return Mono.error(ex);
      }

      logger.debug("Cookie generated: %s".formatted(cookie.getValue()));
      response.getCookies().set(SESSION_COOKIE_NAME, cookie);

      return Mono.empty();
    });
  }

  public static void setSecurityProperties(SwodlrSecurityProperties securityProperties) {
    if (JweSession.securityProperties == null) {
      // Only allow setting once
      JweSession.securityProperties = securityProperties;
    }
  }

  public static Mono<JweSession> load(ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    HttpCookie cookie = request.getCookies().getFirst(SESSION_COOKIE_NAME);
    if (cookie == null) {
      return Mono.empty();
    }

    return load(cookie);
  }

  public static Mono<JweSession> load(HttpCookie sessionCookie) {
    return Mono.defer(() -> {
      JWEObject jweObject;
      try {
        jweObject = JWEObject.parse(sessionCookie.getValue());
        jweObject.decrypt(securityProperties.decrypter());
      } catch (ParseException | JOSEException ex) {
        return Mono.error(ex);
      }
        
      byte[] data = jweObject.getPayload().toBytes();
      if (data == null) {
        return Mono.empty();
      }

      Optional<JweSession> result = JweSession.deserialize(data);
      if (!result.isPresent()) {
        return Mono.empty();
      }
        
      JweSession session = result.get();
      if (!session.isExpired()) {
        return Mono.just(session);
      }
      
      return Mono.empty();
    });
  }

  @Override
  public boolean isExpired() {
    return Instant.now().compareTo(this.expiration) >= 0;
  }

  @Override
  public Instant getCreationTime() {
    return creationTime;
  }

  @Override
  public Instant getLastAccessTime() {
    /*
     * We don't store this info b/c it would be inefficient to 
     * constantly update the field and resend a new cookie so
     * we just say that the last time the session was accessed
     * was now
     */
    return Instant.now();
  }

  @Override
  public void setMaxIdleTime(Duration maxIdleTime) {
    // We don't really use this at the moment so NOP
    return;
  }

  @Override
  public Duration getMaxIdleTime() {
    // Same here - just returning something
    return Duration.ZERO;
  }

  public void setResponse(ServerHttpResponse response) {
    this.response = response;
  }

  private ResponseCookie generateCookie() throws JOSEException {
    JWEObject jwe = generateJwe();
    ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, jwe.serialize())
        .maxAge(Duration.between(Instant.now(), expiration))
        .path("/")
        //.secure(true) - TODO: Set this based on env
        //.httpOnly(true) - TODO: Set this based on env
        .build();

    return cookie;
  }
  
  private JWEObject generateJwe() throws JOSEException {
    JWEHeader header = generateHeader(this.creationTime);
    Payload payload = new Payload(this.serialize());
    JWEObject jwe = new JWEObject(header, payload);

    jwe.encrypt(securityProperties.encrypter());

    return jwe;
  }

  private byte[] serialize() {
    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

    ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
    DeflaterOutputStream deflaterStream = new DeflaterOutputStream(arrayStream, deflater);
    ObjectOutputStream objectStream;

    try {
      objectStream = new ObjectOutputStream(deflaterStream);
      
      // Store response object temporarily so it's not serialized
      ServerHttpResponse response = this.response;
      this.response = null;
      
      objectStream.writeObject(this);
      objectStream.flush();
      objectStream.close();

      // Restore response object
      this.response = response;
    } catch (IOException ex) {
      logger.error("Serialization failed", ex);
      return null;
    }

    return arrayStream.toByteArray();
  }

  private static Optional<JweSession> deserialize(byte[] buffer) {
    ByteArrayInputStream arrayStream = new ByteArrayInputStream(buffer);
    InflaterInputStream inflaterStream = new InflaterInputStream(arrayStream);
    ObjectInputStream objectStream;

    try {
      objectStream = new ObjectInputStream(inflaterStream);
      Object data = objectStream.readObject();
      if (data instanceof JweSession) {
        objectStream.close();
        return Optional.of((JweSession) data);
      }
    } catch (IOException | ClassNotFoundException ex) {
      logger.error("Deserialization failed", ex);
    }

    return Optional.empty();
  }

  private static JWEHeader generateHeader(Instant expiry) {
    JWEHeader.Builder builder
        = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);

    return builder
        .contentType("application/json")
        .customParam("exp", expiry.toString())
        .build();
  }
}
