package gov.nasa.podaac.swodlr.user;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Users")
public class User implements Serializable {
  @Id
  private UUID id;

  @Column(unique = true)
  private String username;

  @ManyToMany
  @JoinTable(
      name = "ProductHistory",
      joinColumns = @JoinColumn(name = "requestedBy"),
      inverseJoinColumns = @JoinColumn(name = "rasterProduct")
  )
  Set<L2RasterProduct> productHistory;

  public User() { }

  public User(String username) {
    id = UUID.randomUUID();
    this.username = username;
  }
  
  public UUID getId() {
    return id;
  }
}
