package gov.nasa.podaac.swodlr.l2rasterproduct;

import gov.nasa.podaac.swodlr.producthistory.ProductHistory;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.user.User;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "L2RasterProducts")
public class L2RasterProduct {
  @Id
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "definitionID", nullable = false)
  private RasterDefinition definition;

  @Column(nullable = false)
  @PositiveOrZero
  private int cycle;

  @Column(nullable = false)
  @PositiveOrZero
  private int pass;

  @Column(nullable = false)
  @PositiveOrZero
  private int scene;

  @OneToMany(mappedBy = "product")
  private Set<Status> statuses;

  @ManyToMany
  @JoinTable(
      name = "ProductHistory",
      joinColumns = @JoinColumn(name = "rasterProductID"),
      inverseJoinColumns = @JoinColumn(name = "requestedByID")
  )
  private Set<User> users;

  @OneToMany(mappedBy = "id.rasterProduct")
  private Set<ProductHistory> history;

  public L2RasterProduct() { }

  public L2RasterProduct(RasterDefinition definition, int cycle, int pass, int scene) {
    this.id = UUID.randomUUID();
    this.definition = definition;
    this.cycle = cycle;
    this.pass = pass;
    this.scene = scene;
  }

  public UUID getId() {
    return id;
  }

  public RasterDefinition getDefinition() {
    return definition;
  }

  public int getCycle() {
    return cycle;
  }

  public int getPass() {
    return pass;
  }

  public int getScene() {
    return scene;
  }
}
