package gov.nasa.podaac.swodlr.product_history;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, ProductHistoryID> {
    
}
