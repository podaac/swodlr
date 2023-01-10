package gov.nasa.podaac.swodlr.l2rasterproduct;

import gov.nasa.podaac.swodlr.Utils;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.user.User;
import gov.nasa.podaac.swodlr.user.UserReference;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class L2RasterProductController {
  @Autowired
  L2RasterProductService l2RasterProductService;

  @Autowired
  L2RasterProductRepository l2RasterProductRepository;

  @MutationMapping
  public Mono<L2RasterProduct> createL2RasterProduct(
      @ContextValue UserReference userRef,
      @Argument UUID definition,
      @Argument int cycle,
      @Argument int scene,
      @Argument int pass
  ) {
    return Mono.defer(() -> {
      User user = userRef.fetch();
      return l2RasterProductService.createL2RasterProduct(user, definition, cycle, scene, pass);
    });
  }

  @SchemaMapping(typeName = "Status", field = "product")
  public L2RasterProduct getStatusForProduct(Status status) {
    return status.getProduct();
  }

  @SchemaMapping(typeName = "User", field = "products")
  public List<L2RasterProduct> getProductsForUser(@ContextValue User user, @Argument UUID after,
      @Argument int limit) {
    if (after == null) {
      after = Utils.NULL_UUID;
    }

    return l2RasterProductRepository.findByUser(user, after, limit);
  }
}
