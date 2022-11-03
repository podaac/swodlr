package gov.nasa.podaac.swodlr.security;

import gov.nasa.podaac.swodlr.security.authentication.handlers.SuccessMessageAuthenticationSuccessHandler;
import gov.nasa.podaac.swodlr.security.authentication.handlers.UserBootstrapAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationSuccessHandler;

@EnableWebFluxSecurity
@Profile({"!test"})
public class WebSecurityConfig {
  @Autowired
  private UserBootstrapAuthenticationSuccessHandler userBootstrapHandler;

  @Autowired
  private SuccessMessageAuthenticationSuccessHandler successHandler;

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
        });

    return http.build();
  }
}
