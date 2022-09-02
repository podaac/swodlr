package gov.nasa.podaac.swodlr.l2_raster_product;

import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import gov.nasa.podaac.swodlr.user.User;

@Entity
@Table(name="L2RasterProducts")
public class L2RasterProduct {
    @Id
    private UUID id;

    @Column(nullable=false)
    private UUID definition;

    @Column(nullable=false)
    private UUID currentStatus;

    @ManyToMany
    @JoinTable(
        name="ProductHistory",
        joinColumns=@JoinColumn(name="rasterProduct"),
        inverseJoinColumns=@JoinColumn(name="requesetedBy")
    )
    private Set<User> user;

    public L2RasterProduct() { }

    public L2RasterProduct(UUID definitionID) {
        this.id = UUID.randomUUID();
        this.definition = definitionID;
    }

    public UUID getID() {
        return id;
    }

    public UUID getDefinition() {
        return definition;
    }

    public UUID getCurrentStatus() {
        return currentStatus;
    }

    public L2RasterProduct setCurrentStatus(UUID currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }
}
