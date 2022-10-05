package gov.nasa.podaac.swodlr.rasterdefinition;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "RasterDefinitions")
public class RasterDefinition {
  @Id
  private UUID id;

  public RasterDefinition() {
    id = UUID.randomUUID();
  }

  public UUID getId() {
    return id;
  }
}
