package gov.nasa.podaac.swodlr.l2rasterproduct;

import gov.nasa.podaac.swodlr.exception.SwodlrException;
import gov.nasa.podaac.swodlr.producthistory.ProductHistory;
import gov.nasa.podaac.swodlr.producthistory.ProductHistoryRepository;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinitionRepository;
import gov.nasa.podaac.swodlr.status.State;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.status.StatusRepository;
import gov.nasa.podaac.swodlr.user.User;
import java.util.UUID;
import javax.transaction.Transactional;
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
  public L2RasterProduct createL2RasterProduct(User user, UUID definitionId) {
    var queryResult = rasterDefinitionRepository.findById(definitionId);
    if (!queryResult.isPresent()) {
      throw new SwodlrException("Definition not found");
    }

    RasterDefinition definition = queryResult.get();
    L2RasterProduct product = new L2RasterProduct(definition);
    Status status = new Status(product, State.NEW);
    ProductHistory history = new ProductHistory(user, product);

    product = l2RasterProductRepository.save(product);
    statusRepository.save(status);
    productHistoryRepository.save(history);

    return product;
  }
}
