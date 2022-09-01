package org.nasa.podaac.swodlr.raster_definition;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RasterDefinition {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private UUID id;
}
