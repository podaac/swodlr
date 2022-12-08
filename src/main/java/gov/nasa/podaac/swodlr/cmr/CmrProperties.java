package gov.nasa.podaac.swodlr.cmr;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("swodlr.cmr")
@ConstructorBinding
public class CmrProperties {
  public final String endpoint;
  public final String pixcConceptId;
  public final String pixcVecConceptId;

  public CmrProperties(
      @NotNull String endpoint,
      @NotNull String pixcConceptId,
      @NotNull String pixcVecConceptId
  ) {
    this.endpoint = endpoint;
    this.pixcConceptId = pixcConceptId;
    this.pixcVecConceptId = pixcVecConceptId;
  }
}
