package gov.nasa.podaac.swodlr;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
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

import gov.nasa.podaac.swodlr.raster_definition.RasterDefinition;
import gov.nasa.podaac.swodlr.raster_definition.RasterDefinitionRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource({"file:./src/main/resources/application.properties", "classpath:application.properties"})
@AutoConfigureHttpGraphQlTester
public class RasterDefinitionTests {
    private RasterDefinition definition;

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private RasterDefinitionRepository rasterDefinitionRepository;

    @BeforeAll
    public void setupDefinition() {
        definition = new RasterDefinition();
        rasterDefinitionRepository.save(definition);
    }

    @AfterAll
    public void deleteDefinition() {
        rasterDefinitionRepository.delete(definition);
    }

    @Test
    public void queryRasterDefinitions() {
        graphQlTester
            .documentName("query/rasterDefinitions")
            .execute()
            .path("rasterDefinitions[*].id")
            .entityList(UUID.class)
            .contains(definition.getID());
    }
}
