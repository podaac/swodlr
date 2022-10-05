package gov.nasa.podaac.swodlr.producthistory;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import gov.nasa.podaac.swodlr.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, ProductHistoryId> {
  Optional<ProductHistory> findByIdRequestedByAndIdRasterProduct(User requestedBy,
      L2RasterProduct rasterProduct);
}
