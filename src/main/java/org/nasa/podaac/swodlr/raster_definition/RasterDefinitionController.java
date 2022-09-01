package org.nasa.podaac.swodlr.raster_definition;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RasterDefinitionController {
    @Autowired
    RasterDefinitionRepository rasterDefinitionRepository;

    @QueryMapping
    List<RasterDefinition> rasterDefinitions() {
        return rasterDefinitionRepository.findAll();
    }
}
