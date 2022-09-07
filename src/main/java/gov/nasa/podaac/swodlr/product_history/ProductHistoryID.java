package gov.nasa.podaac.swodlr.product_history;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProductHistoryID implements Serializable {
    @Column
    private UUID requestedBy;

    @Column
    private UUID rasterProduct;

    public ProductHistoryID() { }

    public ProductHistoryID(UUID requestedBy, UUID rasterProduct) {
        this.requestedBy = requestedBy;
        this.rasterProduct = rasterProduct;
    }

    public UUID getRequestedByID() {
        return requestedBy;
    }

    public UUID getRasterProductID() {
        return rasterProduct;
    }

    @Override
    public boolean equals(Object x) {
        if (!(x instanceof ProductHistoryID))
            return false;
        
        ProductHistoryID other = (ProductHistoryID) x;
        return Objects.equals(requestedBy, other.requestedBy) && Objects.equals(rasterProduct, other.rasterProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedBy, rasterProduct);
    }
}
