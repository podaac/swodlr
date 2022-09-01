package org.nasa.podaac.swodlr.status;

import java.sql.Timestamp;
import java.util.UUID;

public interface StatusView {
    public UUID getID();
    public UUID getProductID();
    public Timestamp getTimestamp();
    public String getStatus();
}
