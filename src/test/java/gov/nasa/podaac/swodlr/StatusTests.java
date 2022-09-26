package gov.nasa.podaac.swodlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.graphql.test.tester.GraphQlTester.Response;
import org.springframework.test.context.TestPropertySource;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProductRepository;
import gov.nasa.podaac.swodlr.raster_definition.RasterDefinition;
import gov.nasa.podaac.swodlr.raster_definition.RasterDefinitionRepository;
import gov.nasa.podaac.swodlr.status.State;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.status.StatusRepository;
import graphql.com.google.common.collect.Lists;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource({"file:./src/main/resources/application.properties", "classpath:application.properties"})
@AutoConfigureHttpGraphQlTester
public class StatusTests {
    private RasterDefinition definition;

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private L2RasterProductRepository l2RasterProductRepository;

    @Autowired
    private RasterDefinitionRepository rasterDefinitionRepository;

    @Value("classpath:frost.txt")
    private Resource frost;

    @BeforeAll
    public void setupDefinition() {
        definition = new RasterDefinition();
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
    public void queryStatus() throws IOException {
        final int PAGES = 2;
        final int PAGE_LIMIT = 5;

        BufferedReader poetryReader = new BufferedReader(new InputStreamReader(frost.getInputStream()));
        List<String> reasons = poetryReader.lines().toList();

        List<State> stateEnums = Lists.newArrayList(State.values());
        int stateIndex = (stateEnums.indexOf(State.NEW) + 1) % stateEnums.size(), reasonIndex = 0;

        /* Setup mock data */
        UUID productID = graphQlTester
            .documentName("mutation/createL2RasterProduct")
            .variable("rasterDefinitionID", definition.getID())
            .execute()
            .path("createL2RasterProduct.id")
            .entity(UUID.class)
            .get();
        
        L2RasterProduct product = l2RasterProductRepository.findById(productID).get();
        LocalDateTime start = LocalDateTime.now();

        for (int i = 0; i < PAGES * PAGE_LIMIT; i++) {
            Status status = new Status(
                product,
                stateEnums.get(stateIndex),
                reasons.get(++reasonIndex)
            );
            statusRepository.save(status);

            stateIndex = (stateIndex + 1) % stateEnums.size();
        }

        /* Query statuses */
        Set<UUID> previouslySeen = new HashSet<>();
        LocalDateTime previousTimestamp = null;
        UUID afterId = null;

        for (int i = 0; i < PAGES; i++) {
            Response response = graphQlTester
                .documentName(i == 0 ? "query/statusByProduct" : "query/statusByPrevious")
                .variable("product", productID)
                .variable("after", afterId)
                .variable("limit", PAGE_LIMIT)
                .execute();

            /* IDs */
            afterId = response
                .path("status[*].id")
                .entityList(UUID.class)
                .hasSize(PAGE_LIMIT)
                .satisfies(ids -> {
                    for (UUID id : ids) {
                        assertFalse(previouslySeen.contains(id));
                        previouslySeen.add(id);
                    }
                })
                .get()
                .get(PAGE_LIMIT - 1);

            /* States */
            List<String> testStates = response
                .path("status[*].state")
                .entityList(String.class)
                .get();

            for (String state : testStates) {
                stateIndex = Math.floorMod(stateIndex - 1, stateEnums.size());
                assertEquals(stateEnums.get(stateIndex).toString(), state);
            }

            /* Reasons */
            List<String> testReasons = response
                .path("status[*].reasons")
                .entityList(String.class)
                .get();

            for (String reason : testReasons) {
                if (reasonIndex == 0) {
                    assertEquals(reason, null);
                } else {
                    assertEquals(reason, stateEnums.get(reasonIndex--).toString());
                }
            }

            /* Timestamps */
            List<LocalDateTime> timestamps = response
                .path("status[*].timestamp")
                .entityList(LocalDateTime.class)
                .hasSize(PAGE_LIMIT)
                .get();

            for (LocalDateTime timestamp : timestamps) {
                if (previousTimestamp != null)
                    assertTrue(previousTimestamp.compareTo(timestamp) > 0, "previousTimestamp: %s, timestamp: %s".formatted(previousTimestamp, timestamp));
                    
                previousTimestamp = timestamp;
                assertTrue(timestamp.compareTo(start) > 0, "timestamp: %s, start: %s".formatted(timestamp, start));
            }

            /* Product */
            // ID
            response
                .path("status[*].product.id")
                .entityList(UUID.class)
                .containsExactly(Collections.nCopies(PAGE_LIMIT, productID).toArray(UUID[]::new));
            
            // Definition
            response
                .path("status[*].product.definition.id")
                .entityList(UUID.class)
                .containsExactly(Collections.nCopies(PAGE_LIMIT, definition.getID()).toArray(UUID[]::new));
        }
    }

    @Test
    public void queryStatusWithInvalidProduct() {
        graphQlTester
            .documentName("query/statusByProduct")
            .variable("product", Utils.NULL_UUID)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertEquals(1, errors.size());

                var error = errors.get(0);
                assertEquals("status", error.getPath());
                assertEquals("DataFetchingException", error.getExtensions().get("classification"));
                assertEquals("Invalid `product` parameter", error.getMessage());
            });
    }

    @Test
    public void queryStatusWithInvalidAfter() {
        graphQlTester
            .documentName("query/statusByPrevious")
            .variable("after", Utils.NULL_UUID)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertEquals(1, errors.size());

                var error = errors.get(0);
                assertEquals("status", error.getPath());
                assertEquals("DataFetchingException", error.getExtensions().get("classification"));
                assertEquals("Invalid `after` parameter", error.getMessage());
            });
    }
}
