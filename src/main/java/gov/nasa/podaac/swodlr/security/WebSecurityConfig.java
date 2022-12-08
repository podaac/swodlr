package gov.nasa.podaac.swodlr.security;

import gov.nasa.podaac.swodlr.security.authentication.handlers.SuccessMessageAuthenticationSuccessHandler;
import gov.nasa.podaac.swodlr.security.authentication.handlers.UserBootstrapAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationSuccessHandler;

@EnableWebFluxSecurity
@Profile({"!test"})
public class WebSecurityConfig {
  private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;
  private final ReactiveOAuth2AuthorizedClientService authorizedClientService; 

  @Autowired
  private UserBootstrapAuthenticationSuccessHandler userBootstrapHandler;

  @Autowired
  private SuccessMessageAuthenticationSuccessHandler successHandler;

  public WebSecurityConfig(ReactiveClientRegistrationRepository clientRegistrationRepository) {
    authorizedClientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
      clientRegistrationRepository, authorizedClientService
    );
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .cors().and()
        .csrf().disable()
        .authorizeExchange(authorize -> {
          authorize.anyExchange().authenticated();
        })
        .oauth2Client().and()
        .oauth2Login((login) -> {
          var authenticationSuccessHandler = new DelegatingServerAuthenticationSuccessHandler(
              userBootstrapHandler, successHandler);

          login.authenticationSuccessHandler(authenticationSuccessHandler);
          login.authorizedClientService(authorizedClientService);
        });

    return http.build();
  }

  @Bean
  public ReactiveOAuth2AuthorizedClientManager authorizedClientManager() {
    return authorizedClientManager;
  }

  @Bean
  public ReactiveOAuth2AuthorizedClientService authorizedClientService() {
    return authorizedClientService;
  }
}
