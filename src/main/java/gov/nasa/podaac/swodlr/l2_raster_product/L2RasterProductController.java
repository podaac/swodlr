package gov.nasa.podaac.swodlr.l2_raster_product;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.user.User;

@Controller
public class L2RasterProductController {
    @Autowired
    L2RasterProductService l2RasterProductService;

    @Autowired
    L2RasterProductRepository l2RasterProductRepository;

    @MutationMapping
    public L2RasterProduct createL2RasterProduct(@ContextValue User user, @Argument UUID definition) {
        return l2RasterProductService.createL2RasterProduct(user, definition);
    }

    @SchemaMapping(typeName="Status", field="product")
    public L2RasterProduct getStatusForProduct(Status status) {
        return status.getProduct();
    }

    @SchemaMapping(typeName="User", field="products")
    public List<L2RasterProduct> getProductsForUser(@ContextValue User user, @Argument UUID after, @Argument int limit) {
        if (after == null) {
            return l2RasterProductRepository.findByUser(user, limit);
        } else {
            return l2RasterProductRepository.findByUser(user, after, limit);
        }
    }
}
