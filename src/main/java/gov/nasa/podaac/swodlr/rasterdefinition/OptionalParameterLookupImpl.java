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

public class OptionalParameterLookupImpl implements OptionalParameterLookup {
  @PersistenceContext
  private EntityManager entityManager;

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
      (:id is NULL OR \"id\" = :id) AND
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
