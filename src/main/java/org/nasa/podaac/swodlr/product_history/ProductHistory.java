package org.nasa.podaac.swodlr.product_history;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class ProductHistory {
    @EmbeddedId
    private ProductHistoryID productHistoryID;
}
