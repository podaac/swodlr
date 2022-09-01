package org.nasa.podaac.swodlr.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Status")
public class Status {
    @Id
    private UUID id;

    @Column(nullable=false)
    private UUID productID;

    @Column
    private UUID previousStatus;

    @Column(nullable=false)
    private Timestamp timestamp;

    @Column(nullable=false)
    private String status;

    public Status() {
        this(null);
    }

    public Status(String status) {
        this.id = UUID.randomUUID();
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
        this.status = status;
    }

    public UUID getID() {
        return id;
    }

    public UUID getProductID() {
        return productID;
    }

    public Status setProductID(UUID productID) {
        this.productID = productID;
        return this;
    }

    public UUID getPreviousStatus() {
        return previousStatus;
    }

    public Status setPreviousStatus(UUID previousStatus) {
        this.previousStatus = previousStatus;
        return this;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
}
