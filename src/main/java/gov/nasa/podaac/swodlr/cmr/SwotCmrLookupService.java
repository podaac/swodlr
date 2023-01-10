package gov.nasa.podaac.swodlr.cmr;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

public interface SwotCmrLookupService {
  public Mono<Map<String, List<String>>> findGranules(int cycle, int pass, int scene);
}
