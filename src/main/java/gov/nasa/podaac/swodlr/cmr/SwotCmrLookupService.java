package gov.nasa.podaac.swodlr.cmr;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

import gov.nasa.podaac.swodlr.exception.SwodlrException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.WebGraphQlClient;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Profile("!test")
public class SwotCmrLookupService {
  private static final List<String> ACCEPTED_FILE_EXTS = List.of("nc");

  private static final Map<String, String> COLLECTION_PREFIXES = Map.ofEntries(
    Map.entry("PIXC", "SWOT_L2_HR_PIXC"),
    Map.entry("PIXCVec", "SWOT_L2_HR_PIXCVec")
  );

  private static final char[] SWATH_DIRECTIONS = new char[]{'L', 'R'};

  private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;
  private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
  private final CmrProperties cmrProperties;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public SwotCmrLookupService(
      ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
      ReactiveOAuth2AuthorizedClientService authorizedClientService,
      CmrProperties cmrProperties
  ) {
    this.authorizedClientManager = authorizedClientManager;
    this.authorizedClientService = authorizedClientService;
    this.cmrProperties = cmrProperties;
  }

  public Mono<Map<String, List<String>>> findGranules(
      @PositiveOrZero int cycle,
      @PositiveOrZero int pass,
      @PositiveOrZero int scene
  ) {
    logger.trace("findGranules cycle: %d, pass: %d, scene: %d");

    Map<String, Object> reqBody = buildReqBody(cycle, pass, scene);
    logger.debug("Request body: " + reqBody.toString());

    return buildGraphQlClient()
        .document("query/granule_lookup")
        .variables(reqBody)
        .execute()
        .map((response) -> parseResponse(response));
  }

  private Map<String, List<String>> parseResponse(ClientGraphQlResponse response) {
    Object data = response.getData();
    if (data != null) {
      logger.debug("Response received: %s", data.toString());
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> pixcResults = (Map<String, Object>) response.field("pixc");
    @SuppressWarnings("unchecked")
    Map<String, Object> pixcVecResults = (Map<String, Object>) response.field("pixcVec");

    Map<String, List<String>> granules = Map.ofEntries(
      Map.entry("pixc", parseResults(pixcResults)),
      Map.entry("pixcVec", parseResults(pixcVecResults))
    );

    return granules;
  }

  private List<String> parseResults(Map<String, Object> results) {
    Integer count = (Integer) results.get("count");
    if (count == 0) {
      throw new SwodlrException("No granules found");
    } else if (count != 16) {
      throw new SwodlrException("Query not returning all granules");
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> items = (List<Map<String, Object>>) results.get("items");

    List<String> granules = new ArrayList<>();

    for (Map<String, Object> granule : items) {
      Float granuleSize = (Float) granule.get("granuleSize");
      logger.info("Granule size: " + granuleSize);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> links = (List<Map<String, Object>>) granule.get("links");
      Optional<String> directLink = getDirectLink(links);

      if (!directLink.isPresent()) {
        throw new SwodlrException("Invalid granule data found during lookup");
      }

      granules.add(directLink.get());
    }

    return granules;
  }

  private List<String> generateTileList(
      final String prefix,
      final int cycle,
      final int pass,
      final int scene
  ) {
    List<String> tiles = new ArrayList<>();

    for (int tile = scene + 1; tile <= scene + 4; tile++) {
      for (char direction : SWATH_DIRECTIONS) {
        tiles.add("%s_%d_%d_%d%c_*".formatted(prefix, cycle, pass, tile, direction));
      }
    }

    return Collections.unmodifiableList(tiles);
  }

  private Map<String, Object> buildReqBody(
      final int cycle,
      final int pass,
      final int scene
  ) {
    Map<String, Object> options = Map.ofEntries(
      Map.entry("options", Map.ofEntries(
        Map.entry("readableGranuleName", Collections.singletonMap("pattern", true))
      ))
    );

    List<String> pixcTileList = generateTileList(COLLECTION_PREFIXES.get("PIXC"), cycle, pass, scene);
    List<String> pixcVecTileList = generateTileList(COLLECTION_PREFIXES.get("PIXCVec"), cycle, pass, scene);
   
    Map<String, Object> pixcParams = Map.ofEntries(
      Map.entry("collectionConceptId", cmrProperties.pixcConceptId),
      Map.entry("readableGranuleName", pixcTileList),
      Map.entry("options", options)
    );

    Map<String, Object> pixcVecParams = Map.ofEntries(
      Map.entry("collectionConceptId", cmrProperties.pixcVecConceptId),
      Map.entry("readableGranuleName", pixcVecTileList),
      Map.entry("options", options)
    );

    Map<String, Object> root = Map.ofEntries(
      Map.entry("pixcParams", pixcParams),
      Map.entry("pixcVecParams", pixcVecParams)
    );

    return root;
  }

  private Optional<String> getDirectLink(List<Map<String, Object>> links) {
    for (var link : links) {
      URL url;

      try {
        url = new URL((String) link.get("URL"));
      } catch (MalformedURLException ex) {
        // Just skip this link - might be a CMR glitch
        logger.debug("Rejected: malformed url - " + link.get("URL"));
        continue;
      }

      if (!url.getProtocol().equals("s3")) {
        logger.debug("Rejected: not an S3 link - " + url.getProtocol());
        continue;
      }

      String path = url.getPath();
      String ext = path.substring(path.lastIndexOf('.') + 1).toLowerCase();

      if (!ACCEPTED_FILE_EXTS.contains(ext)) {
        logger.debug("Rejected: unaccepted file ext - " + ext);
        continue;
      }

      return Optional.of(url.toString());
    }

    return Optional.empty();
  }

  private WebGraphQlClient buildGraphQlClient() {
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
			new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

    EDLAuthorizedClientFilterFunction edlAUthorizedClient =
      new EDLAuthorizedClientFilterFunction(authorizedClientService);

    oauth2Client.setDefaultClientRegistrationId("edl");
    
    WebClient webClient = WebClient.builder()
        .baseUrl(cmrProperties.endpoint)
        .filter(edlAUthorizedClient)
        .filter(oauth2Client)
        .defaultHeaders((headers) -> {
          headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
          headers.set("Client-Id", "PO.DAAC Swodlr");
        })
        .build();

    return HttpGraphQlClient.builder(webClient).build();
  }
}
