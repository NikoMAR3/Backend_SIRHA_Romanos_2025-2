package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Deanery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Dean entities.
 *
 * Provides CRUD operations through MongoRepository and
 * custom queries for searching deans by id, listing all,
 * and finding deans by deanery reference.
 */
@Repository
public interface DeanRepository extends MongoRepository<Dean, String> {

    /**
     * Finds a dean by its id.
     *
     * @param id the dean id
     * @return the matching Dean, or null if not found
     */
    @Query("{ '_id': ?0 }")
    Dean searchDeanById(String id);

    /**
     * Finds deans by deanery id reference.
     *
     * @param deaneryId the deanery id
     * @return list of deans belonging to the deanery
     */
    @Query("{ 'deanery.$id': ?0 }")
    List<Dean> findByDeaneryId(String deaneryId);


    /**
     * Finds a dean by deanery entity reference.
     *
     * @param deanery the deanery entity
     * @return an Optional containing the dean of the deanery, or empty if not found
     */
    Optional<Dean> findByDeanery(Deanery deanery);

    /**
     * Retrieves all deans from the database.
     * @return a list of all deans
     */
    @Override
    List<Dean> findAll();
}
