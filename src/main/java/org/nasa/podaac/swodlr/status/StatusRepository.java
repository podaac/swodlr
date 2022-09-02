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
            ORDER BY timestamp DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<Status> findViewsByProductID(UUID productID, int limit);

    @Query(
        value="""
            SELECT * FROM \"Status\"
            WHERE 
                \"productID\" = (SELECT \"productID\" FROM \"Status\" WHERE id = :#{#after})
                AND
                (timestamp, id) <= (SELECT timestamp, id FROM \"Status\" WHERE id = :#{#after})
            ORDER BY timestamp DESC, id DESC LIMIT :#{#limit}
        """,
        nativeQuery=true
    )
    List<Status> findViewsAfterID(UUID after, int limit);
}
