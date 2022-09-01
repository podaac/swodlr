package org.nasa.podaac.swodlr.status;

import java.util.List;

import org.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StatusController {
    @Autowired
    StatusRepository statusRepository;

    @SchemaMapping(typeName="L2RasterProduct", field="status")
    List<? extends StatusView> L2RasterProduct_status(L2RasterProduct product, @Argument int limit) {
        return statusRepository.findViewsByProductID(product.getID(), limit);
    }
}
