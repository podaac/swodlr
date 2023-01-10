package gov.nasa.podaac.swodlr.cmr;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class EDLAuthorizedClientFilterFunction implements ExchangeFilterFunction {
  private static final String CLIENT_REGISTRATION_ID = "edl";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

  public EDLAuthorizedClientFilterFunction(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
    this.authorizedClientManager = authorizedClientManager;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap(context -> getAuthorizedClient(context))
        .flatMap((OAuth2AuthorizedClient authorizedClient) -> {
          ClientRequest newRequest = ClientRequest.from(request)
            .attributes(oauth2AuthorizedClient(authorizedClient))
            .build();

          return next.exchange(newRequest);
        })
        .switchIfEmpty(Mono.defer(() -> {
          logger.error("Authorized client not found");
          return Mono.empty();
        }));
  }

  private Mono<OAuth2AuthorizedClient> getAuthorizedClient(SecurityContext context) {
    return Mono.defer(() -> {
      OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
          .withClientRegistrationId(CLIENT_REGISTRATION_ID)
          .principal(context.getAuthentication())
          .build();
      return authorizedClientManager.authorize(request);
    });
  }
}
