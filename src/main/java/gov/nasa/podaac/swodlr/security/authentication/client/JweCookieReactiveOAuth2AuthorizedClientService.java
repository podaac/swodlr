package gov.nasa.podaac.swodlr.security.authentication.client;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import reactor.core.publisher.Mono;

public class JweCookieReactiveOAuth2AuthorizedClientService implements ReactiveOAuth2AuthorizedClientService {
  @Override
  public Mono<OAuth2AuthorizedClient> loadAuthorizedClient(String clientRegistrationId,
      String principalName) {
    return loadAuthorizedClientStore()
        .map((authorizedClientStore) -> {
          return authorizedClientStore.authorizedClients.get(clientRegistrationId);
        });
  }

  @Override
  public Mono<Void> saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
    return loadAuthorizedClientStore()
        .flatMap((authorizedClientStore) -> {
          authorizedClientStore.authorizedClients.put(
            authorizedClient.getClientRegistration().getRegistrationId(),
            authorizedClient
          );
          return authorizedClientStore.saveToContext();
        });
  }

  @Override
  public Mono<Void> removeAuthorizedClient(String clientRegistrationId, String principalName) {
    return loadAuthorizedClientStore()
        .flatMap((authorizedClientStore) -> {
          authorizedClientStore.authorizedClients.remove(clientRegistrationId);
          return authorizedClientStore.saveToContext();
        });
  }

  private Mono<JweCookieAuthorizedClientStore> loadAuthorizedClientStore() {
    return JweCookieAuthorizedClientStore
        .loadFromContext()
        .switchIfEmpty(Mono.defer(() -> {
          return Mono.just(new JweCookieAuthorizedClientStore());
        }));
  }
}
