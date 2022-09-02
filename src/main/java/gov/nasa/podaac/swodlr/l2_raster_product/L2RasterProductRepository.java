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
            JOIN \"ProductHistory\" ON \"ProductHistory\".\"rasterProduct\" = \"L2RasterProducts\".id
            WHERE \"ProductHistory\".\"requestedBy\" = :#{#user.getID()}
            ORDER BY \"ProductHistory\".timestamp DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<L2RasterProduct> findByUser(User user, int limit);

    @Query(
        value="""
            SELECT \"L2RasterProducts\".* FROM \"L2RasterProducts\"
            JOIN \"ProductHistory\" ON \"ProductHistory\".\"rasterProduct\" = \"L2RasterProducts\".id
            WHERE
                \"ProductHistory\".\"requestedBy\" = :#{#user.getID()}
                AND
                (\"ProductHistory\".timestamp, \"ProductHistory\".\"rasterProduct\") <= (SELECT timestamp, \"rasterProduct\" FROM \"ProductHistory\" WHERE \"requestedBy\" = :#{#user.getID()} AND \"rasterProduct\" = :#{#after})
            ORDER BY \"ProductHistory\".timestamp DESC, \"ProductHistory\".\"rasterProduct\" DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<L2RasterProduct> findByUser(User user, UUID after, int limit);

    int countByUser(User user);
}
