package gov.nasa.podaac.swodlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProductRepository;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinitionRepository;
import gov.nasa.podaac.swodlr.status.State;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.GraphQlTester.Response;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource({"file:./src/main/resources/application.properties", "classpath:application.properties"})
@AutoConfigureHttpGraphQlTester
public class L2RasterProductTests {
  private RasterDefinition definition;

  @Autowired
  private HttpGraphQlTester graphQlTester;

  @Autowired
  private L2RasterProductRepository l2RasterProductRepository;

  @Autowired
  private RasterDefinitionRepository rasterDefinitionRepository;

  @BeforeAll
  public void setupDefinition() {
    definition = TestUtils.dummyDefinition();
    rasterDefinitionRepository.save(definition);
  }

  @AfterAll
  public void deleteDefinition() {
    rasterDefinitionRepository.delete(definition);
  }

  @AfterEach
  public void deleteProducts() {
    l2RasterProductRepository.deleteAll();
  }

  @Test
  public void createL2RasterProductWithValidDefinition() {
    LocalDateTime start = LocalDateTime.now();

    Random random = new Random();
    int cycle = random.nextInt(Integer.MAX_VALUE);
    int pass = random.nextInt(Integer.MAX_VALUE);
    int scene = random.nextInt(Integer.MAX_VALUE);

    Response response = graphQlTester
        .documentName("mutation/createL2RasterProduct")
        .variable("definition", definition.getId())
        .variable("cycle", cycle)
        .variable("pass", pass)
        .variable("scene", scene)
        .execute();

    /* -- Definition -- */
    response
        .path("createL2RasterProduct.definition.id")
        .entity(UUID.class)
        .isEqualTo(definition.getId());
      
    /* -- Status -- */
    // Timestamp
    response
        .path("createL2RasterProduct.status[*].timestamp")
        .entityList(LocalDateTime.class)
        .hasSize(1)
        .satisfies(timestamps -> {
          var timestamp = timestamps.get(0);
          assertTrue(timestamp.compareTo(start) >= 0, "timestamp: %s, start: %s".formatted(timestamp, start));
        });

    // State
    response
        .path("createL2RasterProduct.status[*].state")
        .entityList(String.class)
        .hasSize(1)
        .containsExactly(State.NEW.toString());
    
    // Reason
    response
        .path("createL2RasterProduct.status[*].reason")
        .entityList(Object.class)
        .containsExactly(new Object[] {null});
  }

  @Test
  public void createL2RasterProductWithInvalidCps() {
    Set<String> invalidParams = new HashSet<>(Set.of(
        "cycle", "pass", "scene"
    ));

    graphQlTester
        .documentName("mutation/createL2RasterProduct")
        .variable("definition", definition.getId())
        .variable("cycle", -1)
        .variable("pass", -1)
        .variable("scene", -1)
        .execute()
        .errors()
        .satisfy(errors -> {
          assertEquals(
              invalidParams.size(),
              errors.size(),
              "%d != %d".formatted(invalidParams.size(), errors.size())
          );

          for (var error : errors) {
            assertEquals("createL2RasterProduct", error.getPath());
            assertEquals("ValidationError", error.getExtensions().get("classification"));
            assertEquals("must be greater than or equal to 0", error.getMessage());

            var prop = error.getExtensions().get("property");
            assertTrue(invalidParams.contains(prop));
            invalidParams.remove(prop);
          }

          assertEquals(0, invalidParams.size());
        });
  }

  @Test
  public void createL2RasterProductWithInvalidDefinition() {
    graphQlTester
        .documentName("mutation/createL2RasterProduct")
        .variable("definition", Utils.NULL_UUID)
        .variable("cycle", 0)
        .variable("pass", 0)
        .variable("scene", 0)
        .execute()
        .errors()
        .satisfy(errors -> {
          assertEquals(1, errors.size());

          var error = errors.get(0);
          assertEquals("createL2RasterProduct", error.getPath());
          assertEquals("DataFetchingException", error.getExtensions().get("classification"));
          assertEquals("Definition not found", error.getMessage());
        });
  }

  @Test
  public void queryCurrentUsersProducts() {
    final int pages = 2;
    final int pageLimit = 5;

    LocalDateTime start = LocalDateTime.now();

    // Create new mock products to fill pages for pagination
    RasterDefinition definition = TestUtils.dummyDefinition();
    rasterDefinitionRepository.save(definition);

    for (int i = 0; i < pageLimit * pages; i++) {
      graphQlTester
          .documentName("mutation/createL2RasterProduct")
          .variable("definition", definition.getId())
          .variable("cycle", i)
          .variable("pass", i)
          .variable("scene", i)
          .executeAndVerify();
    }

    Set<UUID> previouslySeen = new HashSet<>();
    LocalDateTime previousTimestamp = null;
    UUID afterId = null;

    // Iterate through pages
    for (int i = 0; i < pages; i++) {
      Response response = graphQlTester
          .documentName("query/currentUser_products")
          .variable("after", afterId)
          .variable("limit", pageLimit)
          .execute();

      /* -- IDs -- */
      afterId = response
          .path("currentUser.products[*].id")
          .entityList(UUID.class)
          .hasSize(pageLimit)
          .satisfies(ids -> {
            for (UUID id : ids) {
              assertFalse(previouslySeen.contains(id), "Item has duplicated in pagination: %s".formatted(id));
              previouslySeen.add(id);
            }
          })
          .get()
          .get(pageLimit - 1);

      /* -- Definitions -- */
      response
          .path("currentUser.products[*].definition.id")
          .entityList(UUID.class)
          .containsExactly(Collections.nCopies(pageLimit, definition.getId()).toArray(UUID[]::new));

      /* -- Cycle -- */
      response
          .path("currentUser.products[*].cycle")
          .entityList(Integer.class)
          .satisfies(ids -> {
            int j = pages;
            for (int id : ids) {
              assertEquals(--j, id);
            }
          });

      /* -- Pass -- */
      response
          .path("currentUser.products[*].pass")
          .entityList(Integer.class)
          .satisfies(ids -> {
            int j = pages;
            for (int id : ids) {
              assertEquals(--j, id);
            }
          });

      /* -- Scene -- */
      response
          .path("currentUser.products[*].scene")
          .entityList(Integer.class)
          .satisfies(ids -> {
            int j = pages;
            for (int id : ids) {
              assertEquals(--j, id);
            }
          });

      /* -- Statuses -- */
      // State
      response
          .path("currentUser.products[*].status[*].state")
          .entityList(String.class)
          .hasSize(pageLimit)
          .satisfies(states -> states.forEach(state -> assertEquals(State.NEW.toString(), state)));

      // Reason
      response
          .path("currentUser.products[*].status[*].reason")
          .entityList(Object.class)
          .hasSize(pageLimit)
          .satisfies(reasons -> reasons.forEach(reason -> assertEquals(null, reason)));

      // Timestamp
      List<LocalDateTime> timestamps = response
          .path("currentUser.products[*].status[*].timestamp")
          .entityList(LocalDateTime.class)
          .hasSize(pageLimit)
          .get();
      
      for (LocalDateTime timestamp : timestamps) {
        if (previousTimestamp != null) {
          assertTrue(previousTimestamp.compareTo(timestamp) > 0, "previousTimestamp: %s, timestamp: %s".formatted(previousTimestamp, timestamp));
        }

        previousTimestamp = timestamp;
        assertTrue(timestamp.compareTo(start) > 0, "timestamp: %s, start: %s".formatted(timestamp, start));
      }
    }
  }
}
