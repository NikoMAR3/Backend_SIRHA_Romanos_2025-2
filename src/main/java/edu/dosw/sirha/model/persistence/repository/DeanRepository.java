package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Dean;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Returns all deans.
     *
     * @return list of all deans
     */
    @Query("{}")
    List<Dean> searchAllDeans();

    /**
     * Finds deans by deanery id reference.
     *
     * @param deaneryId the deanery id
     * @return list of deans belonging to the deanery
     */
    @Query("{ 'deanery.$id': ?0 }")
    List<Dean> findByDeaneryId(String deaneryId);

}
