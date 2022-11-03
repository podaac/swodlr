package gov.nasa.podaac.swodlr.security.authentication;

import com.nimbusds.oauth2.sdk.ErrorObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class EdlOauth2UserService implements
    ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
  private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP
      = new ParameterizedTypeReference<Map<String, Object>>() { };

  @Override
  public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest)
      throws OAuth2AuthenticationException {
    return Mono.defer(() -> {
      WebClient.RequestHeadersSpec<?> requestHeaders;
      try {
        requestHeaders = buildWebClient(userRequest);
      } catch (URISyntaxException ex) {
        throw new RuntimeException("User info URI malformed", ex);
      }

      return requestHeaders.retrieve()
          .onStatus(HttpStatus::isError, response -> 
            response.bodyToMono(JSONObject.class).map(errorResponse -> {
              var rawErrorObject = ErrorObject.parse(errorResponse);
              var oauth2Error = new OAuth2Error(
                  rawErrorObject.getCode(),
                  rawErrorObject.getDescription(),
                  rawErrorObject.getURI().toString()
              );

              throw new OAuth2AuthenticationException(oauth2Error);
            })
          )
          .bodyToMono(STRING_OBJECT_MAP)
          .map((Map<String, Object> attrs) -> {
            GrantedAuthority authority = new OAuth2UserAuthority(attrs);
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(authority);
            OAuth2AccessToken token = userRequest.getAccessToken();
            for (String scope : token.getScopes()) {
              authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
            }

            return new DefaultOAuth2User(authorities, attrs, "uid");
          });
    });
  }

  private WebClient.RequestHeadersSpec<?> buildWebClient(OAuth2UserRequest userRequest)
      throws URISyntaxException {
    String userInfoEndpoint = userRequest.getClientRegistration().getProviderDetails()
        .getUserInfoEndpoint().getUri();
    URI usersInfoUri = UriComponentsBuilder
        .fromHttpUrl(userInfoEndpoint)
        .replacePath((String) userRequest.getAdditionalParameters().get("endpoint"))
        .queryParam("client_id", userRequest.getClientRegistration().getClientId())
        .build().toUri();

    OAuth2AccessToken token = userRequest.getAccessToken();

    if (token.getTokenType() != OAuth2AccessToken.TokenType.BEARER) {
      throw new RuntimeException("Only bearer token auth is supported on EDL");
    }

    return WebClient.create()
        .get()
        .uri(usersInfoUri)
        .accept(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .headers(headers -> headers.setBearerAuth(token.getTokenValue()));
  }
}
