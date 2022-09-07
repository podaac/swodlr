package gov.nasa.podaac.swodlr.l2_raster_product;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nasa.podaac.swodlr.exception.SwodlrException;
import gov.nasa.podaac.swodlr.product_history.ProductHistory;
import gov.nasa.podaac.swodlr.product_history.ProductHistoryRepository;
import gov.nasa.podaac.swodlr.raster_definition.RasterDefinition;
import gov.nasa.podaac.swodlr.raster_definition.RasterDefinitionRepository;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.status.StatusRepository;
import gov.nasa.podaac.swodlr.status.state.State;
import gov.nasa.podaac.swodlr.user.User;

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
            throw new SwodlrException("Definition not found");

        RasterDefinition definition = queryResult.get();
        Status status = new Status(State.NEW);
        L2RasterProduct product = new L2RasterProduct(definition);
        ProductHistory history = new ProductHistory(user, product);

        status.setProduct(product);

        product = l2RasterProductRepository.save(product);
        statusRepository.save(status);
        productHistoryRepository.save(history);

        return product;
    }
}
