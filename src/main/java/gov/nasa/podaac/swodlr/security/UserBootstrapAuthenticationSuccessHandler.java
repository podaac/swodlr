package gov.nasa.podaac.swodlr.security;

import gov.nasa.podaac.swodlr.user.User;
import gov.nasa.podaac.swodlr.user.UserReference;
import gov.nasa.podaac.swodlr.user.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserBootstrapAuthenticationSuccessHandler
    implements ServerAuthenticationSuccessHandler {

  @Autowired
  private UserRepository userRepository;

  @Override
  public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
      Authentication authentication) {
    return Mono.defer(() -> {
      return webFilterExchange.getExchange().getSession()
          .doOnNext((session) -> {
            var principle = authentication.getPrincipal();
            if (principle instanceof DefaultOAuth2User oauth2User) {
              // the name attribute is the EDL username which acts as a uid
              String username = oauth2User.getName();
              Optional<User> result = userRepository.findByUsername(username);

              if (result.isPresent()) {
                UserReference userReference = new UserReference(result.get());
                session.getAttributes().put("user", userReference);
              } else {
                User user = new User(username);
                userRepository.save(user);

                UserReference userReference = new UserReference(user);
                session.getAttributes().put("user", userReference);
              }
            }
          })
          .then();
    });
  }
}
