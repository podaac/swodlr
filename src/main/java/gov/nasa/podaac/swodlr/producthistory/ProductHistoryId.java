package gov.nasa.podaac.swodlr.producthistory;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class ProductHistoryId implements Serializable {
  @ManyToOne(optional = false)
  @JoinColumn(name = "requestedByID", nullable = false)
  private User requestedBy;

  @ManyToOne(optional = false)
  @JoinColumn(name = "rasterProductID", nullable = false)
  private L2RasterProduct rasterProduct;

  public ProductHistoryId() { }

  public ProductHistoryId(User requestedBy, L2RasterProduct rasterProduct) {
    this.requestedBy = requestedBy;
    this.rasterProduct = rasterProduct;
  }

  public User getRequestedBy() {
    return requestedBy;
  }

  public L2RasterProduct getRasterProduct() {
    return rasterProduct;
  }

  @Override
  public boolean equals(Object x) {
    if (!(x instanceof ProductHistoryId)) {
      return false;
    }
    
    ProductHistoryId other = (ProductHistoryId) x;
    return Objects.equals(requestedBy.getId(), other.requestedBy.getId())
        && Objects.equals(rasterProduct.getId(), other.rasterProduct.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestedBy.getId(), rasterProduct.getId());
  }
}
