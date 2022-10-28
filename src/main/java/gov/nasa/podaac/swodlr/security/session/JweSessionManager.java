package gov.nasa.podaac.swodlr.security.session;

import gov.nasa.podaac.swodlr.security.SwodlrSecurityProperties;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

@Component("webSessionManager")
public class JweSessionManager implements WebSessionManager {
  private static final String SESSION_CACHE_KEY = "jweSessionCache";

  public JweSessionManager(@Autowired SwodlrSecurityProperties securityProperties) {
    JweSession.setSecurityProperties(securityProperties);
  }

  @Override
  public Mono<WebSession> getSession(ServerWebExchange exchange) {
    // Attempt to load from cache
    Map<String, Object> attributes = exchange.getAttributes();
    if (attributes.containsKey(SESSION_CACHE_KEY)) {
      return Mono.just((JweSession) attributes.get(SESSION_CACHE_KEY));
    }

    return JweSession.load(exchange)
        .switchIfEmpty(createSession(exchange))
        .doOnNext(session -> attributes.put(SESSION_CACHE_KEY, session))
        .doOnNext(session -> exchange.getResponse().beforeCommit(() -> session.save()));
  }

  private Mono<WebSession> createSession(ServerWebExchange exchange) {
    return Mono.defer(() -> {
      return Mono.just(new JweSession(exchange.getResponse()));
    });
  }
}
