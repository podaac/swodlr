package gov.nasa.podaac.swodlr.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
      .cors().and()
      .csrf().and()
      .authorizeExchange(authorize -> {
        authorize.anyExchange().authenticated();
      })
      .oauth2Client().and()
      .oauth2Login();

    return http.build();
  }
}
