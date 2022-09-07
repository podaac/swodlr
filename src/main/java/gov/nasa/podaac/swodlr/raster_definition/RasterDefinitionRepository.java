package gov.nasa.podaac.swodlr.raster_definition;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RasterDefinitionRepository extends JpaRepository<RasterDefinition, UUID> {
    List<RasterDefinition> findById(RasterDefinition definition);
}
