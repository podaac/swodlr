package gov.nasa.podaac.swodlr.l2rasterproduct;

import gov.nasa.podaac.swodlr.cmr.SwotCmrLookupService;
import gov.nasa.podaac.swodlr.exception.SwodlrException;
import gov.nasa.podaac.swodlr.producthistory.ProductHistory;
import gov.nasa.podaac.swodlr.producthistory.ProductHistoryRepository;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinition;
import gov.nasa.podaac.swodlr.rasterdefinition.RasterDefinitionRepository;
import gov.nasa.podaac.swodlr.status.State;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.status.StatusRepository;
import gov.nasa.podaac.swodlr.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

  @Autowired
  SwotCmrLookupService swotCmrLookupService;

  @Transactional
  public Mono<L2RasterProduct> createL2RasterProduct(
      User user,
      UUID definitionId,
      int cycle,
      int pass,
      int scene
  ) {
    return Mono
      .defer(() -> {
        var result = rasterDefinitionRepository.findById(definitionId);
        if (!result.isPresent()) {
          return Mono.error(new SwodlrException("Definition not found"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("definition", result.get());

        return Mono.just(data);
      })
      .flatMap((Map<String, Object> data) -> {
        return swotCmrLookupService.findGranules(cycle, pass, scene)
            .map((granules) -> {
              data.put("granules", granules);
              return data;
            });
      })
      .map((Map<String, Object> data) -> {
        RasterDefinition definition = (RasterDefinition) data.get("definition");

        L2RasterProduct product = new L2RasterProduct(definition, cycle, pass, scene);
        Status status = new Status(product, State.NEW);
        ProductHistory history = new ProductHistory(user, product);

        product = l2RasterProductRepository.save(product);
        statusRepository.save(status);
        productHistoryRepository.save(history);

        return product;
      });
  }
}
