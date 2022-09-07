package gov.nasa.podaac.swodlr.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.nasa.podaac.swodlr.l2_raster_product.L2RasterProduct;
import gov.nasa.podaac.swodlr.status.state.State;

@Entity
@Table(name="Status")
public class Status {
    @Id
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="productID", nullable=false)
    private L2RasterProduct product;

    @Column(nullable=false)
    private Timestamp timestamp;

    @Column(nullable=false)
    @Type(type="gov.nasa.podaac.swodlr.status.state.StateType")
    private State state;

    @Column
    private String reason;

    public Status() {
        this(null);
    }

    public Status(State state) {
        this(state, null);
    }

    public Status(State state, String reason) {
        this.id = UUID.randomUUID();
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
        this.state = state;
        this.reason = reason;
    }

    public UUID getID() {
        return id;
    }

    public L2RasterProduct getProduct() {
        return product;
    }

    public Status setProduct(L2RasterProduct product) {
        this.product = product;
        return this;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public State getState() {
        return state;
    }

    public String getReason() {
        return reason;
    }
}
