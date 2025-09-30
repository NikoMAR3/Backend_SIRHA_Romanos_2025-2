package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Deanery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Deanery entities.
 *
 * Provides basic CRUD operations through MongoRepository and
 * custom methods following the same pattern as other repositories.
 */
@Repository
public interface DeaneryRepository extends MongoRepository<Deanery, String> {

    /**
     * Finds a deanery by its name.
     * @param deaneryName the name of the deanery
     * @return an Optional containing the found Deanery, or empty if not found
     */
    Optional<Deanery> findByDeaneryName(String deaneryName);

    /**
     * Searches for a deanery by its unique identifier.
     * @param id the unique identifier of the deanery
     * @return an Optional containing the found Deanery, or empty if not found
     */
    Optional<Deanery> findDeaneryById(String id);

    /**
     * Retrieves all deaneries from the database.
     * @return a list of all Deaneries
     */
    @Override
    List<Deanery> findAll();
}
