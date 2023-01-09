package gov.nasa.podaac.swodlr.security.session;

import gov.nasa.podaac.swodlr.security.AbstractJweCookieStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

public class JweSession extends AbstractJweCookieStore implements WebSession {
  public static final String SESSION_COOKIE_NAME = "session";
  private static final Logger logger = LoggerFactory.getLogger(JweSession.class);

  private UUID id;
  private transient ServerHttpResponse response;
  private final Map<String, Object> attributes;

  JweSession(ServerHttpResponse response) {
    super(SESSION_COOKIE_NAME);

    this.id = UUID.randomUUID();
    this.response = response;
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
      } catch (Exception ex) {
        logger.error("Failed to generate session cookie", ex);
        return Mono.error(ex);
      }

      logger.debug("Cookie generated: %s".formatted(cookie.getValue()));
      response.getCookies().set(SESSION_COOKIE_NAME, cookie);

      return Mono.empty();
    });
  }

  public static Mono<JweSession> load(ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    HttpCookie cookie = request.getCookies().getFirst(SESSION_COOKIE_NAME);
    if (cookie == null) {
      return Mono.empty();
    }

    return Mono.justOrEmpty(loadCookie(JweSession.class, cookie));
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
}
