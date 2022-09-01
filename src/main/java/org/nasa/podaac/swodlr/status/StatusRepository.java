package org.nasa.podaac.swodlr.status;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StatusRepository extends JpaRepository<Status, UUID> {
    @Query(
        value="""
            SELECT * FROM \"Status\"
            WHERE \"productID\" = :#{#productID}
            ORDER BY \"timestamp\" DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<Status> findViewsByProductID(UUID productID, int limit);

    @Query(
        value="""
            SELECT CAST(id,  AS VARCHAR) * FROM Status
            WHERE (id, productID) >= (:#{#productID}, :#{#startingID})
            ORDER BY ProductHistory.timestamp DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<StatusView> findViewsByProductIDStartingWith(UUID startingID, UUID productID, int limit);
}
