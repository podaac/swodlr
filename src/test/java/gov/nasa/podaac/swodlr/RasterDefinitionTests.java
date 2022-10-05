package gov.nasa.podaac.swodlr;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nasa.podaac.swodlr.rasterdefinition.GridType;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinitionRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource({"file:./src/main/resources/application.properties", "classpath:application.properties"})
@AutoConfigureHttpGraphQlTester
public class RasterDefinitionTests {
  @Autowired
  private HttpGraphQlTester graphQlTester;

  @Autowired
  private RasterDefinitionRepository rasterDefinitionRepository;

  @BeforeAll
  public void clearDefinitions() {
    rasterDefinitionRepository.deleteAll();
  }

  @Test
  public void queryRasterDefinitions() {
    var utmDefinition = new RasterDefinition();
    utmDefinition.outputGranuleExtentFlag = true;
    utmDefinition.outputSamplingGridType = GridType.UTM;
    utmDefinition.rasterResolution = 10000;
    utmDefinition.utmZoneAdjust = -1;
    utmDefinition.mgrsBandAdjust = 1;

    var geoDefinition = new RasterDefinition();
    geoDefinition.outputGranuleExtentFlag = false;
    geoDefinition.outputSamplingGridType = GridType.GEO;
    geoDefinition.rasterResolution = 3;

    rasterDefinitionRepository.save(utmDefinition);
    rasterDefinitionRepository.save(geoDefinition);

    Set<UUID> validUuids = new HashSet<>();
    validUuids.add(utmDefinition.getId());
    validUuids.add(geoDefinition.getId());

    graphQlTester
        .documentName("query/rasterDefinitions")
        .execute()
        .path("rasterDefinitions[*].id")
        .entityList(UUID.class)
        .satisfies(uuidList -> {
          for (UUID uuid : uuidList) {
            assertTrue(validUuids.contains(uuid));
          }
        });
  }
}
