package gov.nasa.podaac.swodlr.status;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;

@Entity
@Table(name="Status")
public class Status {
    @Id
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="productID", nullable=false)
    private L2RasterProduct product;

    @Column(nullable=false)
    private LocalDateTime timestamp;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private State state;

    @Column
    private String reason;

    public Status() {
        this(null, null);
    }

    public Status(L2RasterProduct product, State state) {
        this(product, state, null);
    }

    public Status(L2RasterProduct product, State state, String reason) {
        this.id = UUID.randomUUID();
        this.product = product;
        this.timestamp = LocalDateTime.now();
        this.state = state;
        this.reason = reason;
    }

    public UUID getID() {
        return id;
    }

    public L2RasterProduct getProduct() {
        return product;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public State getState() {
        return state;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "%s (id: %s, productID: %s, timestamp: %s, state: %s, reason: %s)".formatted(super.toString(), id, product, timestamp, state, reason);
    }
}
