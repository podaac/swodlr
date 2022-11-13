package gov.nasa.podaac.swodlr;

import java.util.HashMap;
import java.util.Map;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

public class SsmProperties extends PropertySource<Object> {
  private static final String SOURCE_NAME = "ssmProperties";
  private static final String PATH = System.getProperties().getProperty(
      "SSM_PATH", 
      "/service/swodlr/app"
  );

  private final Map<String, String> properties = new HashMap<>();

  public SsmProperties() {
    super(SOURCE_NAME);

    SsmClient ssmClient = SsmClient.builder()
        .credentialsProvider(null)
        .build();

    GetParametersByPathResponse res = ssmClient.getParametersByPath((request) -> {
      request.path(PATH);
    });

    for (Parameter param : res.parameters()) {
      properties.put(param.name(), param.value());
    }
  }

  @Override
  public Object getProperty(String name) {
    return properties.get(name);
  }
}
