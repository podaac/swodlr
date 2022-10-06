package gov.nasa.podaac.swodlr.rasterdefinition;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RasterDefinitionController {
  @Autowired
  RasterDefinitionRepository rasterDefinitionRepository;

  @QueryMapping
  List<RasterDefinition> rasterDefinitions(
      @Argument UUID id,
      @Argument Boolean outputGranuleExtentFlag,
      @Argument GridType outputSamplingGridType,
      @Argument Integer rasterResolution,
      @Argument Integer utmZoneAdjust,
      @Argument Integer mgrsBandAdjust
  ) {
    return rasterDefinitionRepository.findByParameter(
      id, outputGranuleExtentFlag, outputSamplingGridType, rasterResolution,
      utmZoneAdjust, mgrsBandAdjust);
  }

  @SchemaMapping(typeName = "L2RasterProduct", field = "definition")
  RasterDefinition getDefinitionForL2RasterProduct(L2RasterProduct rasterProduct) {
    return rasterProduct.getDefinition();
  }
}
