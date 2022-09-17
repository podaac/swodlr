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
public class UserTests {
    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Test
    public void queryCurrentUser() {
        graphQlTester
            .documentName("query/currentUser")
            .execute()
            .path("currentUser.id")
            .entity(String.class)
            .isEqualTo("fee1dc78-0604-4fa6-adae-0b4b55440e7d");
    }
}
