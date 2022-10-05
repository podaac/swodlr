package gov.nasa.podaac.swodlr.producthistory;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ProductHistory")
public class ProductHistory {
  @EmbeddedId
  private ProductHistoryId id;

  @Column(nullable = false)
  LocalDateTime timestamp;

  public ProductHistory() { }

  public ProductHistory(User requestedBy, L2RasterProduct rasterProduct) {
    id = new ProductHistoryId(requestedBy, rasterProduct);
    timestamp = LocalDateTime.now();
  }

  public User getRequestedBy() {
    return id.getRequestedBy();
  }

  public L2RasterProduct getRasterProduct() {
    return id.getRasterProduct();
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
