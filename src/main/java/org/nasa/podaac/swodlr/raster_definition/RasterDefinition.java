package org.nasa.podaac.swodlr.raster_definition;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="RasterDefinitions")
public class RasterDefinition {
    @Id
    private UUID id;

    public RasterDefinition() {
        id = UUID.randomUUID();
    }

    public UUID getID() {
        return id;
    }
}
