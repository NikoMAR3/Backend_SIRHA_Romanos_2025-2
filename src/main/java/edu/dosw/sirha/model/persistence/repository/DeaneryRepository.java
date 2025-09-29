package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Deanery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing Deanery entities.
 *
 * Provides basic CRUD operations through MongoRepository and
 * a custom method to find a deanery by its name.
 */
public interface DeaneryRepository extends MongoRepository<Deanery, String> {

    /**
     * Finds a deanery by its name.
     *
     * @param deaneryName the name of the deanery
     * @return the matching Deanery, or null if not found
     */
    Deanery findByDeaneryName(String deaneryName);

}
