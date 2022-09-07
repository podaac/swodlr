package gov.nasa.podaac.swodlr.l2_raster_product;

import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.nasa.podaac.swodlr.raster_definition.RasterDefinition;
import gov.nasa.podaac.swodlr.status.Status;
import gov.nasa.podaac.swodlr.user.User;

@Entity
@Table(name="L2RasterProducts")
public class L2RasterProduct {
    @Id
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="definitionID", nullable=false)
    private RasterDefinition definition;

    @OneToMany(mappedBy="product")
    private Set<Status> statuses;

    @ManyToMany
    @JoinTable(
        name="ProductHistory",
        joinColumns=@JoinColumn(name="rasterProduct"),
        inverseJoinColumns=@JoinColumn(name="requestedBy")
    )
    private Set<User> users;

    public L2RasterProduct() { }

    public L2RasterProduct(RasterDefinition definition) {
        this.id = UUID.randomUUID();
        this.definition = definition;
    }

    public UUID getID() {
        return id;
    }

    public RasterDefinition getDefinition() {
        return definition;
    }
}
