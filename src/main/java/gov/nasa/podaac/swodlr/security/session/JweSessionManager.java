package gov.nasa.podaac.swodlr.security.session;

import gov.nasa.podaac.swodlr.security.SwodlrSecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

@Component("webSessionManager")
public class JweSessionManager implements WebSessionManager {
  public JweSessionManager(@Autowired SwodlrSecurityProperties securityProperties) {
    JweSession.setSecurityProperties(securityProperties);
  }

  @Override
  public Mono<WebSession> getSession(ServerWebExchange exchange) {
    return JweSession.load(exchange)
        .switchIfEmpty(createSession(exchange))
        .doOnNext(session -> {
          session.setResponse(exchange.getResponse());
          exchange.getResponse().beforeCommit(() -> session.save());
        })
        .cast(WebSession.class)
        .cache();
  }

  private Mono<JweSession> createSession(ServerWebExchange exchange) {
    return Mono.defer(() -> {
      return Mono.just(new JweSession(exchange.getResponse()));
    });
  }
}
