package gov.nasa.podaac.swodlr.rasterdefinition;

import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringNVarcharType;
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
      \"id\" = COALESCE(:id, \"id\") AND
      \"outputGranuleExtentFlag\" = COALESCE(:outputGranuleExtentFlag, \"outputGranuleExtentFlag\") AND
      \"outputSamplingGridType\" = COALESCE(:outputSamplingGridType, \"outputSamplingGridType\") AND
      \"rasterResolution\" = COALESCE(:rasterResolution, \"rasterResolution\") AND
      \"utmZoneAdjust\" = COALESCE(:utmZoneAdjust, \"utmZoneAdjust\") AND
      \"mgrsBandAdjust\" = COALESCE(:mgrsBandAdjust, \"mgrsBandAdjust\")
      ORDER BY id
        """;
    
    Session session = entityManager.unwrap(Session.class);
    Query<RasterDefinition> query = session.createNativeQuery(statement, RasterDefinition.class);
    query.setParameter("id", id, UUIDCharType.INSTANCE);
    query.setParameter("outputGranuleExtentFlag", outputGranuleExtentFlag, BooleanType.INSTANCE);
    query.setParameter("outputSamplingGridType", outputSamplingGridType, StringNVarcharType.INSTANCE);
    query.setParameter("rasterResolution", rasterResolution, IntegerType.INSTANCE);
    query.setParameter("utmZoneAdjust", utmZoneAdjust, IntegerType.INSTANCE);
    query.setParameter("mgrsBandAdjust", mgrsBandAdjust, IntegerType.INSTANCE);

    return query.getResultList();
  }
}
