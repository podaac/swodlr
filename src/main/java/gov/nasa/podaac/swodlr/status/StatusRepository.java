package gov.nasa.podaac.swodlr.status;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;

public interface StatusRepository extends JpaRepository<Status, UUID> {
    @Query(
        value="""
            SELECT * FROM \"Status\"
            WHERE 
                \"productID\" = :#{#product.getID()}
                AND
                (
                    :#{#after} = CAST('00000000-0000-0000-0000-000000000000' as uuid)
                    OR
                    (timestamp, id) < (SELECT timestamp, id FROM \"Status\" WHERE id = :#{#after})
                )
            ORDER BY timestamp DESC, id DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<Status> findByProductId(L2RasterProduct product, UUID after, int limit);
}
