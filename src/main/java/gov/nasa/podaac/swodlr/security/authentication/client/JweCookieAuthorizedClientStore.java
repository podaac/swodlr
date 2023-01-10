package gov.nasa.podaac.swodlr.security.authentication.client;

import gov.nasa.podaac.swodlr.security.AbstractJweCookieStore;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import reactor.core.publisher.Mono;

public class JweCookieAuthorizedClientStore extends AbstractJweCookieStore {
  private static final String AUTH_CLIENTS_COOKIE_NAME = "auth_clients";

  public final Map<String, OAuth2AuthorizedClient> authorizedClients = new HashMap<>();

  public JweCookieAuthorizedClientStore() {
    super(AUTH_CLIENTS_COOKIE_NAME);
  }

  public static Mono<JweCookieAuthorizedClientStore> loadFromContext() {
    return loadFromContext(JweCookieAuthorizedClientStore.class, AUTH_CLIENTS_COOKIE_NAME);
  }

  public Mono<Void> saveToContext() {
    return saveToContext(this);
  }
}
