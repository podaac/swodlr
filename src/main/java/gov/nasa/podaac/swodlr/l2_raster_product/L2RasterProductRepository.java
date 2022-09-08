package gov.nasa.podaac.swodlr.l2_raster_product;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gov.nasa.podaac.swodlr.user.User;

public interface L2RasterProductRepository extends JpaRepository<L2RasterProduct, UUID> {
    @Query(
        value="""
            SELECT \"L2RasterProducts\".* FROM \"L2RasterProducts\"
            JOIN \"ProductHistory\" ON \"ProductHistory\".\"rasterProductID\" = \"L2RasterProducts\".id
            WHERE
                \"ProductHistory\".\"requestedByID\" = :#{#user.getID()}
                AND
                (
                    :#{#after} IS NULL
                    OR
                    (\"ProductHistory\".timestamp, \"ProductHistory\".\"rasterProductID\") < (SELECT timestamp, \"rasterProductID\" FROM \"ProductHistory\" WHERE \"requestedByID\" = :#{#user.getID()} AND \"rasterProductID\" = :#{#after})
                )
            ORDER BY \"ProductHistory\".timestamp DESC, \"ProductHistory\".\"rasterProductID\" DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<L2RasterProduct> findByUser(User user, UUID after, int limit);

    List<L2RasterProduct> findById(L2RasterProduct product);
}
