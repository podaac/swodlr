package gov.nasa.podaac.swodlr.cmr;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.WebGraphQlClient;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import gov.nasa.podaac.swodlr.exception.SwodlrException;
import reactor.core.publisher.Mono;

@Service
public class CmrLookupService {
  @Autowired
  private CmrProperties cmrProperties;

  @Autowired
  private OAuth2AuthorizedClientService authorizedClientService;

  public Mono<List<GranuleMetadata>> findGranules(
      @NotNull OAuth2User principal,
      @PositiveOrZero int cycle,
      @PositiveOrZero int pass,
      @PositiveOrZero int scene
  ) {
    buildWebClient(principal)
        .document("query/granule_lookup")
        .variable(null, principal)
        .execute()
        .flatMap((response) -> {
          
        });
    return null;
  }
  
  private WebGraphQlClient buildGraphQlClient(OAuth2User principal) {
    OAuth2AuthorizedClient authClient = authorizedClientService.loadAuthorizedClient("edl", principal.getName());
    return HttpGraphQlClient.builder()
        .url(cmrProperties.endpoint)
        .headers((headers) -> {
          headers.setBearerAuth(authClient.getAccessToken().getTokenValue());
          headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
          headers.set("Client-Id", "PO.DAAC Swodlr");
        })
        .build();
  }
}
