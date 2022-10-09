package gov.nasa.podaac.swodlr.rasterdefinition;

import java.util.List;
import java.util.UUID;

public interface RasterDefinitionQuery {
  List<RasterDefinition> findByParameter(
      UUID id,
      Boolean outputGranuleExtentFlag,
      GridType outputSamplingGridType,
      Integer rasterResolution,
      Integer utmZoneAdjust,
      Integer mgrsBandAdjust
  );
}
