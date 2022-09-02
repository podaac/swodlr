package gov.nasa.podaac.swodlr.product_history;

import java.io.Serializable;
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
}
