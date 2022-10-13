package gov.nasa.podaac.swodlr.rasterdefinition;

import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.UUIDCharType;

public class RasterDefinitionQueryImpl implements RasterDefinitionQuery {
  @PersistenceContext
  private EntityManager entityManager;


  /*
   * This implementation is utilized to workaround an JPA issue with the
   * PostgreSQL dialect in Hibernate where Hibernate does not parameterize
   * null values as their original datatype and instead parameterizes as
   * bytea's. This is a known issue of the PostgreSQL driver/dialect
   * 
   * This can lead to the exceptions:
   *    - "ERROR: could not determine data type of parameter $1"
   *    - "ERROR: operator does not exist: uuid = bytea"
   * 
   * Solutions researched include casting values, however, by casting
   * in the query, we loose the benefits of parameterization in our
   * prepared statements and create a statement which is harder to read.
   * Workarounds such as COALESCE and testing other PostgreSQL dialects
   * available in Hibernate were tried, but were found to either not
   * solve the issue or still require the use of CASTs in queries
   * 
   * The hope is to one day remove this code in favor of Spring Data JPA
   * queries, pending the PostgreSQL/Hibernate teams' cooperation with
   * one another
   * 
   * Relevant discussions:
   *    - https://stackoverflow.com/a/64223435
   *    - https://stackoverflow.com/a/62680643
   *    - https://github.com/pgjdbc/pgjdbc/issues/247#issuecomment-78213991
   */
  @Override
  public List<RasterDefinition> findByParameter(
      UUID id,
      Boolean outputGranuleExtentFlag,
      GridType outputSamplingGridType,
      Integer rasterResolution,
      Integer utmZoneAdjust,
      Integer mgrsBandAdjust
  ) {  
    String statement = """
      SELECT * FROM \"RasterDefinitions\" WHERE
      (:id is NULL OR \"id\" = CAST(:id as UUID)) AND
      (:outputGranuleExtentFlag is NULL OR \"outputGranuleExtentFlag\" = :outputGranuleExtentFlag) AND
      (:outputSamplingGridType is NULL OR \"outputSamplingGridType\" = :outputSamplingGridType) AND
      (:rasterResolution is NULL OR \"rasterResolution\" = :rasterResolution) AND
      (:utmZoneAdjust is NULL OR \"utmZoneAdjust\" = :utmZoneAdjust) AND
      (:mgrsBandAdjust is NULL OR \"mgrsBandAdjust\" = :mgrsBandAdjust)
      ORDER BY id
        """;
    
    Session session = entityManager.unwrap(Session.class);
    Query<RasterDefinition> query = session.createNativeQuery(statement, RasterDefinition.class);
    query.setParameter("id", id, UUIDCharType.INSTANCE);
    query.setParameter("outputGranuleExtentFlag", outputGranuleExtentFlag, BooleanType.INSTANCE);
    query.setParameter("outputSamplingGridType", outputSamplingGridType != null
        ? outputSamplingGridType.toString() : null, StringType.INSTANCE);
    query.setParameter("rasterResolution", rasterResolution, IntegerType.INSTANCE);
    query.setParameter("utmZoneAdjust", utmZoneAdjust, IntegerType.INSTANCE);
    query.setParameter("mgrsBandAdjust", mgrsBandAdjust, IntegerType.INSTANCE);

    return query.getResultList();
  }
}
