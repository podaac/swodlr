package gov.nasa.podaac.swodlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.graphql.test.tester.GraphQlTester.Response;
import org.springframework.test.context.TestPropertySource;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProductRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource({"file:./src/main/resources/application.properties", "classpath:application.properties"})
@AutoConfigureHttpGraphQlTester
public class L2RasterProductTests {
    private static final UUID VALID_DEFINITION_ID = UUID.fromString("a6d12de3-5f76-4e2d-9a42-1f2ab7f9ed7c");

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Autowired
    L2RasterProductRepository l2RasterProductRepository;

    @AfterEach
    public void resetDatabase() {
        l2RasterProductRepository.deleteAll();
    }

    @Test
    public void createL2RasterProductWithValidDefinition() {
        LocalDateTime start = LocalDateTime.now();

        Response response = graphQlTester
            .documentName("mutation/createL2RasterProduct")
            .variable("rasterDefinitionID", VALID_DEFINITION_ID)
            .execute();

        /* -- Definition -- */
        response
            .path("createL2RasterProduct.definition.id")
            .entity(String.class)
            .isEqualTo(VALID_DEFINITION_ID.toString());
            
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
            .containsExactly("NEW");
        
        // Reason
        response
            .path("createL2RasterProduct.status[*].reason")
            .entityList(Object.class)
            .containsExactly(new Object[] {null});
    }

    @Test
    public void createL2RasterProductWithInvalidDefinition() {
        graphQlTester
            .documentName("mutation/createL2RasterProduct")
            .variable("rasterDefinitionID", Utils.NULL_UUID)
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
        final int PAGES = 2;
        final int PAGE_LIMIT = 5;

        LocalDateTime start = LocalDateTime.now();

        // Create new mock products to fill pages for pagination
        for (int i = 0; i < PAGE_LIMIT * PAGES; i++) {
            graphQlTester
                .documentName("mutation/createL2RasterProduct")
                .variable("rasterDefinitionID", VALID_DEFINITION_ID)
                .executeAndVerify();
        }

        Set<UUID> previouslySeen = new HashSet<>();
        LocalDateTime previousTimestamp = null;
        UUID afterId = null;

        // Iterate through pages
        for (int i = 0; i < PAGES; i++) {
            Response response = graphQlTester
                .documentName("query/currentUser_products")
                .variable("after", afterId)
                .variable("limit", PAGE_LIMIT)
                .execute();

            /* -- IDs -- */
            afterId = response
                .path("currentUser.products[*].id")
                .entityList(UUID.class)
                .hasSize(PAGE_LIMIT)
                .satisfies(ids -> {
                    for (UUID id : ids) {
                        assertFalse(previouslySeen.contains(id), "Item has duplicated in pagination: %s".formatted(id));
                        previouslySeen.add(id);
                    }
                })
                .get()
                .get(PAGE_LIMIT - 1);

            /* -- Definitions -- */
            response
                .path("currentUser.products[*].definition.id")
                .entityList(UUID.class)
                .containsExactly(Collections.nCopies(PAGE_LIMIT, VALID_DEFINITION_ID).toArray(UUID[]::new));

            /* -- Statuses -- */
            
            // State
            response
                .path("currentUser.products[*].status[*].state")
                .entityList(String.class)
                .hasSize(PAGE_LIMIT)
                .satisfies(states -> states.forEach(state -> assertEquals("NEW", state)));

            // Reason
            response
                .path("currentUser.products[*].status[*].reason")
                .entityList(Object.class)
                .hasSize(PAGE_LIMIT)
                .satisfies(reasons -> reasons.forEach(reason -> assertEquals(null, reason)));

            // Timestamp
            List<LocalDateTime> timestamps = response
                .path("currentUser.products[*].status[*].timestamp")
                .entityList(LocalDateTime.class)
                .hasSize(PAGE_LIMIT)
                .get();
            
            for (LocalDateTime timestamp : timestamps) {
                if (previousTimestamp != null)
                    assertTrue(previousTimestamp.compareTo(timestamp) > 0, "previousTimestamp: %s, timestamp: %s".formatted(previousTimestamp, timestamp));
                
                previousTimestamp = timestamp;
                assertTrue(timestamp.compareTo(start) > 0, "timestamp: %s, start: %s".formatted(timestamp, start));
            }
        }

    }
}
