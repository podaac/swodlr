package org.nasa.podaac.swodlr.l2_raster_product;

import java.util.UUID;

import javax.transaction.Transactional;

import org.nasa.podaac.swodlr.product_history.ProductHistory;
import org.nasa.podaac.swodlr.product_history.ProductHistoryRepository;
import org.nasa.podaac.swodlr.raster_definition.RasterDefinition;
import org.nasa.podaac.swodlr.raster_definition.RasterDefinitionRepository;
import org.nasa.podaac.swodlr.status.Status;
import org.nasa.podaac.swodlr.status.StatusRepository;
import org.nasa.podaac.swodlr.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class L2RasterProductService {
    @Autowired
    L2RasterProductRepository l2RasterProductRepository;

    @Autowired
    RasterDefinitionRepository rasterDefinitionRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    ProductHistoryRepository productHistoryRepository;

    @Transactional
    public L2RasterProduct createL2RasterProduct(User user, UUID definitionID) {
        var queryResult = rasterDefinitionRepository.findById(definitionID);
        if (!queryResult.isPresent())
            throw new RuntimeException("Definition not found"); // TODO: Move to own class?

        RasterDefinition definition = queryResult.get();
        Status status = new Status("Initial creation");
        L2RasterProduct product = new L2RasterProduct(definition.getID());
        ProductHistory history = new ProductHistory(product.getID(), user.getID());

        status.setProductID(product.getID());
        product.setCurrentStatus(status.getID());

        product = l2RasterProductRepository.save(product);
        statusRepository.save(status);
        productHistoryRepository.save(history);

        return product;
    }
}
