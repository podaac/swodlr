package gov.nasa.podaac.swodlr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.StandardEnvironment;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SwodlrApp {
  public static void main(String[] args) {
    StandardEnvironment env = new StandardEnvironment();
    env.getPropertySources().addFirst(new SsmProperties());

    SpringApplication app = new SpringApplication(SwodlrApp.class);
    app.setEnvironment(env);
    app.run(args);
  }
}
