package gov.nasa.podaac.swodlr.security;

import gov.nasa.podaac.swodlr.user.User;
import gov.nasa.podaac.swodlr.user.UserReference;
import java.util.Collections;
import java.util.Optional;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GraphQlUserInjector implements WebGraphQlInterceptor {
  @Override
  public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
    return Mono.deferContextual((context) -> {
      if (context.hasKey(ServerWebExchange.class)) {
        return context.get(ServerWebExchange.class).getSession().flatMap((session) -> {
          UserReference userReference = session.getAttribute("user");
          if (userReference == null) {
            return chain.next(request);
          }

          Optional<User> result = userReference.fetch();
          if (result.isPresent()) {
            request.configureExecutionInput((executionInput, builder) -> {
              builder.graphQLContext(Collections.singletonMap("user", result.get()));
              return builder.build();
            });
          }

          return chain.next(request);
        });
      }

      return chain.next(request);
    });
  }
}
