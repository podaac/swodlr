package gov.nasa.podaac.swodlr.security.authentication.handlers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SuccessMessageAuthenticationSuccessHandler
    implements ServerAuthenticationSuccessHandler {
  public static final byte[] PLAIN_MESSAGE
      = "Authentication successful - please resend your request"
        .getBytes(StandardCharsets.UTF_8);

  @Override
  public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
      Authentication authentication) {
    return Mono.defer(() -> {
      ServerHttpRequest req = webFilterExchange.getExchange().getRequest();
      ServerHttpResponse res = webFilterExchange.getExchange().getResponse();

      if (acceptsTextPlain(req.getHeaders().getAccept())) {
        DataBuffer buffer = res.bufferFactory().wrap(PLAIN_MESSAGE);
        res.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        return res.writeWith(Mono.just(buffer));
      }

      return Mono.empty();
    });
  }

  private boolean acceptsTextPlain(List<MediaType> accepts) {
    for (MediaType type : accepts) {
      if (type.includes(MediaType.TEXT_PLAIN)) {
        return true;
      }
    }

    return false;
  }
}
