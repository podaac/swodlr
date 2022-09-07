package gov.nasa.podaac.swodlr.raster_definition;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import gov.nasa.podaac.swodlr.exception.SwodlrException;
import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;

@Controller
public class RasterDefinitionController {
    @Autowired
    RasterDefinitionRepository rasterDefinitionRepository;

    @QueryMapping
    List<RasterDefinition> rasterDefinitions() {
        return rasterDefinitionRepository.findAll();
    }

    @SchemaMapping(typeName="L2RasterProduct", field="definition")
    RasterDefinition L2RasterProduct_definition(L2RasterProduct rasterProduct) {
        var result = rasterDefinitionRepository.findById(rasterProduct.getDefinition());
        if (!result.isPresent())
            throw new SwodlrException("Definition not found");
        
        return result.get();
    }
}
