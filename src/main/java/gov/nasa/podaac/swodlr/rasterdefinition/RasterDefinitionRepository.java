package gov.nasa.podaac.swodlr.rasterdefinition;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RasterDefinitionRepository extends JpaRepository<RasterDefinition, UUID>,
    OptionalParameterLookup {
  List<RasterDefinition> findById(RasterDefinition definition);
}
