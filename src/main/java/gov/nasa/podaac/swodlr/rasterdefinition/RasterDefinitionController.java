package gov.nasa.podaac.swodlr.rasterdefinition;

import gov.nasa.podaac.swodlr.l2rasterproduct.L2RasterProduct;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RasterDefinitionController {
  @Autowired
  RasterDefinitionRepository rasterDefinitionRepository;

  @QueryMapping
  List<RasterDefinition> rasterDefinitions() {
    return rasterDefinitionRepository.findAll();
  }

  @SchemaMapping(typeName = "L2RasterProduct", field = "definition")
  RasterDefinition getDefinitionForL2RasterProduct(L2RasterProduct rasterProduct) {
    return rasterProduct.getDefinition();
  }
}
