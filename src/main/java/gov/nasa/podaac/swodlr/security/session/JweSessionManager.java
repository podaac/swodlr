package gov.nasa.podaac.swodlr.security.session;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import gov.nasa.podaac.swodlr.security.SwodlrSecurityConfig;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

@Component
public class JweSessionManager implements WebSessionManager {
  private static final String SESSION_COOKIE_NAME = "session";

  @Autowired
  private SwodlrSecurityConfig securityConfig;

  @Override
  public Mono<WebSession> getSession(ServerWebExchange exchange) {
    return retrieveFromCookie(exchange)
        .switchIfEmpty(createSession(exchange))
        .cast(WebSession.class);
  }

  private Mono<JweSession> retrieveFromCookie(ServerWebExchange exchange) {
    return Mono.defer(() -> {
      ServerHttpRequest request = exchange.getRequest();

      HttpCookie cookie = request.getCookies().getFirst(SESSION_COOKIE_NAME);
      if (cookie == null || cookie.getValue().length() == 0) {
        return Mono.empty();
      }

      JWEObject jweObject;
      try {
        jweObject = JWEObject.parse(cookie.getValue());
        jweObject.decrypt(securityConfig.decrypter());
      } catch (ParseException | JOSEException ex) {
        return Mono.error(ex);
      }
      
      Map<String, Object> json = jweObject.getPayload().toJSONObject();
      if (json == null) {
        return Mono.empty();
      }

      ServerHttpResponse response = exchange.getResponse();
      return Mono.just(JweSession.fromMap(json, response));
    });
  }
  
  private Mono<JweSession> createSession(ServerWebExchange exchange) {
    return Mono.defer(() -> {
      return Mono.just(new JweSession(exchange.getResponse()));
    });
  }

  private static class JweSession implements WebSession {
    private static final String ID_FIELD = "id";
    private static final String CREATIONTIME_FIELD = "creationTime";
    private static final String EXPIRATION_FIELD = "expiration";
    private static final String ATTRIBUTES_FIELD = "attributes";

    private UUID id;
    private final Instant creationTime;
    private final Instant expiration;
    private final ServerHttpResponse response;
    private final Map<String, Object> attributes;

    @Autowired
    private SwodlrSecurityConfig securityConfig;

    public JweSession(ServerHttpResponse response) {
      this.id = UUID.randomUUID();
      this.creationTime = Instant.now();
      this.expiration = this.creationTime.plus(securityConfig.sessionLength());
      this.response = response;
      this.attributes = new ConcurrentHashMap<>();
    }

    private JweSession(
        UUID id,
        Instant creationTime,
        Instant expiration,
        Map<String, Object> attributes,
        ServerHttpResponse response
    ) {
      this.id = id;
      this.creationTime = creationTime;
      this.expiration = expiration;
      this.attributes = new ConcurrentHashMap<>(attributes);
      this.response = response;
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
      return generateCookie()
        .doOnNext((cookie) -> {
          response.getCookies().set(SESSION_COOKIE_NAME, cookie);
        }).then();
    }
  
    @Override
    public boolean isExpired() {
      return Instant.now().compareTo(this.expiration) <= 0;
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

    private Mono<ResponseCookie> generateCookie() {
      return generateJwe()
          .flatMap((jwe) -> {
            Duration maxAge = Duration.between(Instant.now(), expiration);
            ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, jwe.serialize())
                .maxAge(Duration.between(Instant.now(), expiration))
                //.secure(true) - TODO: Set this based on env
                //.httpOnly(true) - TODO: Set this based on env
                .build();

            return Mono.just(cookie);
          });
    }
    
    private Mono<JWEObject> generateJwe() {
      return Mono.defer(() -> {
        JWEHeader header = generateHeader(this.creationTime);
        Payload payload = new Payload(toMap(this));
        JWEObject jwe = new JWEObject(header, payload);

        try {
          jwe.encrypt(securityConfig.encrypter());
        } catch (JOSEException ex) {
          Mono.error(ex);
        }

        return Mono.just(new JWEObject(header, payload));
      });
    }

    public static Map<String, Object> toMap(JweSession session) {
      Map<String, Object> data = new HashMap<>();
      data.put(ID_FIELD, session.id.toString());
      data.put(CREATIONTIME_FIELD, session.creationTime.toString());
      data.put(EXPIRATION_FIELD, session.expiration.toString());
      data.put(ATTRIBUTES_FIELD, session.attributes);

      return Collections.unmodifiableMap(data);
    }

    public static JweSession fromMap(Map<String, Object> json, ServerHttpResponse response) {
      return new JweSession(
        UUID.fromString((String) json.get(ID_FIELD)),
        Instant.parse((String) json.get(CREATIONTIME_FIELD)),
        Instant.parse((String) json.get(EXPIRATION_FIELD)),
        (Map<String, Object>) json.get(ATTRIBUTES_FIELD),
        response
      );
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
}
