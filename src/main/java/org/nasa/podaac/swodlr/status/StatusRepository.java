package org.nasa.podaac.swodlr.status;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, UUID> {
    
}
