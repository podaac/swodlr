package gov.nasa.podaac.swodlr;

import gov.nasa.podaac.swodlr.rasterdefinition.GridType;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;

public class TestUtils {
  static final RasterDefinition dummyDefinition() {
    RasterDefinition definition = new RasterDefinition();
    definition.outputGranuleExtentFlag = true;
    definition.outputSamplingGridType = GridType.UTM;
    definition.rasterResolution = 1000;
    definition.utmZoneAdjust = 1;
    definition.mgrsBandAdjust = -1;

    return definition;
  }
}
