package gov.nasa.podaac.swodlr.cmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Profile("test")
public class MockSwotCmrLookupService implements SwotCmrLookupService {
  private static final char[] SWATH_DIRECTIONS = new char[] { 'L', 'R' };
  private static final Map<String, String> COLLECTION_PREFIXES = Map.ofEntries(
      Map.entry("PIXC", "SWOT_L2_HR_PIXC"),
      Map.entry("PIXCVec", "SWOT_L2_HR_PIXCVec"));

  @Override
  public Mono<Map<String, List<String>>> findGranules(int cycle, int pass, int scene) {
    return Mono.defer(() -> {
      Map<String, List<String>> granuleMap = new HashMap<String, List<String>>();
      for (Entry<String, String> prefix : COLLECTION_PREFIXES.entrySet()) {
        List<String> granules = new ArrayList<>(8);
        for (char direction : SWATH_DIRECTIONS) {
          for (int tile = scene + 1; tile <= scene + 4; tile++) {
            String granuleName = "%s_%03d_%03d_%03d%c_20220403T003904_20220403T003914_PGA2_01"
                .formatted(prefix.getValue(), cycle, pass, tile, direction);
            String dummyUrl = "s3://dummyurl/%s.nc".formatted(granuleName);

            granules.add(dummyUrl);
          }
        }

        granuleMap.put(prefix.getKey(), Collections.unmodifiableList(granules));
      }

      return Mono.just(Collections.unmodifiableMap(granuleMap));
    });
  }
}
