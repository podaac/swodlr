package gov.nasa.podaac.swodlr.cmr;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class EDLAuthorizedClientFilterFunction implements ExchangeFilterFunction {
  private static final String CLIENT_REGISTRATION_ID = "edl";

  private ReactiveOAuth2AuthorizedClientService authorizedClientService;

  public EDLAuthorizedClientFilterFunction(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
    this.authorizedClientService = authorizedClientService;
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
    });
  }

  private Mono<OAuth2AuthorizedClient> getAuthorizedClient(SecurityContext context) {
    return Mono.defer(() -> {
      String principalName = context.getAuthentication().getName();
      return authorizedClientService.loadAuthorizedClient(CLIENT_REGISTRATION_ID, principalName);
    });
  }
}
