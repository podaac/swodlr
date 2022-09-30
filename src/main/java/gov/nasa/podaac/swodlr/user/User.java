package gov.nasa.podaac.swodlr.user;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Users")
public class User {
  @Id
  private UUID id;

  @ManyToMany
  @JoinTable(
      name = "ProductHistory",
      joinColumns = @JoinColumn(name = "requestedBy"),
      inverseJoinColumns = @JoinColumn(name = "rasterProduct")
  )
  Set<L2RasterProduct> productHistory;
  
  public UUID getId() {
    return id;
  }
}
