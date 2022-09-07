package gov.nasa.podaac.swodlr.product_history;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;

@Entity
@Table(name="ProductHistory")
public class ProductHistory {
    @EmbeddedId
    private ProductHistoryID productHistoryID;

    public ProductHistory() { }

    public ProductHistory(User requestedBy, L2RasterProduct rasterProduct) {
        productHistoryID = new ProductHistoryID(requestedBy, rasterProduct);
    }

    public User getRequestedBy() {
        return productHistoryID.getRequestedBy();
    }

    public L2RasterProduct getRasterProduct() {
        return productHistoryID.getRasterProduct();
    }
}
