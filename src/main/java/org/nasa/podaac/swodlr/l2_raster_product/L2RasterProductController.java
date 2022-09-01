package org.nasa.podaac.swodlr.l2_raster_product;

import java.util.UUID;

import org.nasa.podaac.swodlr.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class L2RasterProductController {
    @Autowired
    L2RasterProductService l2RasterProductService;

    @MutationMapping
    public L2RasterProduct createL2RasterProduct(@ContextValue User user, @Argument UUID definition) {
        return l2RasterProductService.createL2RasterProduct(user, definition);
    }
}
