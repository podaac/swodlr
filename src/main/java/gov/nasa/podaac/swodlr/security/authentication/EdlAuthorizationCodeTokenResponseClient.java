package gov.nasa.podaac.swodlr.security.authentication;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class EdlAuthorizationCodeTokenResponseClient
    extends WebClientReactiveAuthorizationCodeTokenResponseClient {
  public EdlAuthorizationCodeTokenResponseClient() {
    super();

    this.setWebClient(WebClient
        .builder()
        .filter((req, next) -> {
          String auth = req.headers().getFirst("Authorization");
          if (auth == null) {
            return Mono.error(new RuntimeException("Authorization not found"));
          }

          String[] authParams = new String(
              Base64.getDecoder().decode(auth.substring("Basic ".length())
          ), StandardCharsets.UTF_8).split(":");
          String username = URLDecoder.decode(authParams[0], StandardCharsets.UTF_8);
          String password = URLDecoder.decode(authParams[1], StandardCharsets.UTF_8);

          ClientRequest newReq = ClientRequest
              .from(req)
              .headers(headers -> headers.setBasicAuth(username, password))
              .build();

          return next.exchange(newReq);
        })
        .build());
  }
}
