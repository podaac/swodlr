package gov.nasa.podaac.swodlr.product_history;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, ProductHistoryID> {
    Optional<ProductHistory> findByIdRequestedByAndIdRasterProduct(User requestedBy, L2RasterProduct rasterProduct);
}
