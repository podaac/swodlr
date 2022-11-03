package gov.nasa.podaac.swodlr.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Profile({"!test"})
public class WebSecurityConfig {
  @Autowired
  private UserBootstrapAuthenticationSuccessHandler userBootstrapAuthenticationSuccessHandler;

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
          login.authenticationSuccessHandler(userBootstrapAuthenticationSuccessHandler);
        });

    return http.build();
  }
}
