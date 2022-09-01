package org.nasa.podaac.swodlr.l2_raster_product;

import java.util.List;
import java.util.UUID;

import org.nasa.podaac.swodlr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface L2RasterProductRepository extends JpaRepository<L2RasterProduct, UUID> {
    @Query(
        value="""
            SELECT L2RasterProducts.* FROM L2RasterProducts
            JOIN ProductHistory ON ProductHistory.rasterProduct = L2RasterProducts.id
            WHERE ProductHistory.requestedBy = :#{#user.id}
            ORDER BY ProductHistory.timestamp DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<L2RasterProduct> findByUser(User user, int limit);

    @Query(
        value="""
            SELECT L2RasterProducts.* FROM L2RasterProducts
            JOIN ProductHistory ON ProductHistory.rasterProduct = L2RasterProducts.id
            WHERE ProductHistory.requestedBy = :#{#user.id}
            ORDER BY ProductHistory.timestamp DEC LIMIT :#{#limit}
            OFFSET :#{#after}
        """,
        nativeQuery=true
    )
    List<L2RasterProduct> findByUser(User user, UUID after, int limit);

    int countByUser(User user);
}
