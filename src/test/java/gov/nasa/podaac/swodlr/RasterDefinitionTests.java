package gov.nasa.podaac.swodlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nasa.podaac.swodlr.rasterdefinition.GridType;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinitionRepository;
import graphql.com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

  @BeforeEach
  public void clearDefinitions() {
    rasterDefinitionRepository.deleteAll();
  }

  @Test
  public void queryRasterDefinitionsWithoutArgs() {
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

  @Test
  public void queryRasterDefinitionsWithArgs() {
    final int numDefinitions = 20;
    final Random random = new Random();
    final List<GridType> gridTypes = Lists.newArrayList(GridType.values());
    final Map<UUID, RasterDefinition> definitions = new HashMap<>();

    final String[] parameters = {"id", "outputGranuleExtentFlag", "outputSamplingGridType", "rasterResolution", "utmZoneAdjust", "mgrsBandAdjust"};

    for (int i = 0; i < numDefinitions; i++) {
      RasterDefinition definition = new RasterDefinition();
      definition.outputGranuleExtentFlag = random.nextBoolean();
      definition.outputSamplingGridType = gridTypes.get(random.nextInt(2));
      definition.rasterResolution = random.nextInt(10000 - 2) + 3;
      
      if (definition.outputSamplingGridType == GridType.UTM) {
        definition.utmZoneAdjust = random.nextInt(3) - 1;
        definition.mgrsBandAdjust = random.nextInt(3) - 1;;
      }

      definitions.put(definition.getId(), definition);
      rasterDefinitionRepository.save(definition);
    }

    for (RasterDefinition definition : definitions.values()) {
      for (String paramName : parameters) {
        var paramVal = getDefinitionField(paramName, definition);
        if (paramVal == null) {
          // Skip when value is null b/c it doesn't filter
          return;
        }

        graphQlTester
            .documentName("query/rasterDefinitions")
            .variable(paramName, getDefinitionField(paramName, definition))
            .execute()
            .path("rasterDefinitions[*].id")
            .entityList(UUID.class)
            .satisfies(uuidList -> {
              assertTrue(uuidList.contains(definition.getId()));

              for (UUID uuid : uuidList) {
                var testVal = getDefinitionField(paramName, definitions.get(uuid));
                assertEquals(paramVal, testVal, "%s: %s != %s".formatted(paramName, paramVal, testVal));
              }
            });
      }
    }
  }

  private Object getDefinitionField(String name, RasterDefinition definition) {
    switch (name) {
      case "id":
        return definition.getId();
      case "outputGranuleExtentFlag":
        return definition.outputGranuleExtentFlag;
      case "outputSamplingGridType":
        return definition.outputSamplingGridType;
      case "rasterResolution":
        return definition.rasterResolution;
      case "utmZoneAdjust":
        return definition.utmZoneAdjust;
      case "mgrsBandAdjust":
        return definition.mgrsBandAdjust;
      default:
        // We shouldn't end up here
        assert false;
        return null;
    }
  }
}
