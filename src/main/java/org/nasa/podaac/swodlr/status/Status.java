package org.nasa.podaac.swodlr.status;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Status {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private UUID id;

    public UUID getUUID() {
        return id;
    }
}
