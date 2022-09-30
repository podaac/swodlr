package gov.nasa.podaac.swodlr.security;

import gov.nasa.podaac.swodlr.user.UserRepository;
import java.util.Collections;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationInterceptor implements WebGraphQlInterceptor {
  private static final UUID TEMP_USER_ID = UUID.fromString("fee1dc78-0604-4fa6-adae-0b4b55440e7d");

  @Autowired
  private UserRepository userRepository;

  @Override
  public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
    request.configureExecutionInput((executionInput, builder) -> {
      var user = userRepository.findById(TEMP_USER_ID);

      if (user.isPresent()) {
        builder.graphQLContext(Collections.singletonMap("user", user.get()));
      }

      return builder.build();
    });
    return chain.next(request);
  }
}
