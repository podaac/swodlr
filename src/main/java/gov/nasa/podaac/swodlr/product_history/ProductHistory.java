package gov.nasa.podaac.swodlr.product_history;

import java.util.UUID;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="ProductHistory")
public class ProductHistory {
    @EmbeddedId
    private ProductHistoryID productHistoryID;

    public ProductHistory() { }

    public ProductHistory(UUID rasterProduct, UUID requestedBy) {
        productHistoryID = new ProductHistoryID(requestedBy, rasterProduct);
    }
}
