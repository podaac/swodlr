package gov.nasa.podaac.swodlr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource({"file:./src/main/resources/application.properties", "classpath:application.properties"})
@AutoConfigureHttpGraphQlTester
public class RasterDefinitionTests {
    @Autowired
    HttpGraphQlTester graphQlTester;

    @Test
    public void queryRasterDefinitions() {
        graphQlTester
            .documentName("query/rasterDefinitions")
            .execute()
            .path("rasterDefinitions[*].id")
            .entityList(String.class)
            .containsExactly("a6d12de3-5f76-4e2d-9a42-1f2ab7f9ed7c");
    }
}
