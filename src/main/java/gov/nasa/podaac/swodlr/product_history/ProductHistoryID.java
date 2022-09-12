package gov.nasa.podaac.swodlr.product_history;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;

@Embeddable
public class ProductHistoryID implements Serializable {
    @ManyToOne(optional=false)
    @JoinColumn(name="requestedByID", nullable=false)
    private User requestedBy;

    @ManyToOne(optional=false)
    @JoinColumn(name="rasterProductID", nullable=false)
    private L2RasterProduct rasterProduct;

    public ProductHistoryID() { }

    public ProductHistoryID(User requestedBy, L2RasterProduct rasterProduct) {
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
        if (!(x instanceof ProductHistoryID))
            return false;
        
        ProductHistoryID other = (ProductHistoryID) x;
        return Objects.equals(requestedBy.getID(), other.requestedBy.getID()) && Objects.equals(rasterProduct.getID(), other.rasterProduct.getID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedBy.getID(), rasterProduct.getID());
    }
}
