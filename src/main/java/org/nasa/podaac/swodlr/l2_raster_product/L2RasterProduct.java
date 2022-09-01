package org.nasa.podaac.swodlr.l2_raster_product;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class L2RasterProduct {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable=false)
    private UUID definition;

    @Column(nullable=false)
    private UUID currentStatus;
}
