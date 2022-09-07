package gov.nasa.podaac.swodlr.status;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;

@Controller
public class StatusController {
    @Autowired
    StatusRepository statusRepository;

    @QueryMapping
    List<Status> statusByProduct(@Argument UUID product, @Argument int limit) {
        return statusRepository.findViewsByProductID(product, limit);
    }

    @QueryMapping
    List<Status> statusByPrevious(@Argument UUID after, @Argument int limit) {
        return statusRepository.findViewsAfterID(after, limit);
    }

    @SchemaMapping(typeName="L2RasterProduct", field="status")
    List<Status> getStatusForL2RasterProduct(L2RasterProduct product, @Argument int limit) {
        return statusRepository.findViewsByProductID(product.getID(), limit);
    }
}
