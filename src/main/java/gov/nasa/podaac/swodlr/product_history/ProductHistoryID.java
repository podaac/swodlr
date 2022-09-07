package gov.nasa.podaac.swodlr.product_history;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import graphql.com.google.common.base.Objects;

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
        return requestedBy.equals(other.requestedBy) && rasterProduct.equals(other.rasterProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(requestedBy, rasterProduct);
    }
}
