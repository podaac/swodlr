package gov.nasa.podaac.swodlr.l2rasterproduct;

import gov.nasa.podaac.swodlr.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface L2RasterProductRepository extends JpaRepository<L2RasterProduct, UUID> {
  @Query(
      value = """
        SELECT \"L2RasterProducts\".* FROM \"L2RasterProducts\"
        JOIN \"ProductHistory\" ON \"ProductHistory\".\"rasterProductID\" = \"L2RasterProducts\".id
        WHERE
          \"ProductHistory\".\"requestedByID\" = :#{#user.getId()}
          AND
          (
            :#{#after} = CAST('00000000-0000-0000-0000-000000000000' as uuid)
            OR
            (\"ProductHistory\".timestamp, \"ProductHistory\".\"rasterProductID\") < (SELECT timestamp, \"rasterProductID\" FROM \"ProductHistory\" WHERE \"requestedByID\" = :#{#user.getId()} AND \"rasterProductID\" = :#{#after})
          )
        ORDER BY \"ProductHistory\".timestamp DESC, \"ProductHistory\".\"rasterProductID\" DESC LIMIT :#{#limit}
      """,
      nativeQuery = true
  )
  List<L2RasterProduct> findByUser(User user, UUID after, int limit);

  List<L2RasterProduct> findById(L2RasterProduct product);
}
