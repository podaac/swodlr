package gov.nasa.podaac.swodlr.product_history;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;

@Entity
@Table(name="ProductHistory")
public class ProductHistory {
    @EmbeddedId
    private ProductHistoryID id;

    @Column(nullable=false)
    Timestamp timestamp;

    public ProductHistory() { }

    public ProductHistory(User requestedBy, L2RasterProduct rasterProduct) {
        id = new ProductHistoryID(requestedBy, rasterProduct);
        timestamp = Timestamp.valueOf(LocalDateTime.now());
    }

    public User getRequestedBy() {
        return id.getRequestedBy();
    }

    public L2RasterProduct getRasterProduct() {
        return id.getRasterProduct();
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
