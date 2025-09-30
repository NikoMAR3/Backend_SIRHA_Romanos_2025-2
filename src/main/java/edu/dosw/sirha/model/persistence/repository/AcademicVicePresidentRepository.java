package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.AcademicVicePresident;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AcademicVicePresident entity operations.
 * Extends MongoRepository to provide basic CRUD operations and custom query methods
 */
@Repository
public interface AcademicVicePresidentRepository extends MongoRepository<AcademicVicePresident, String> {

    /**
     * Finds an academic vice president by their unique identifier.
     * @param id the unique identifier of the academic vice president
     * @return an Optional containing the found AcademicVicePresident, or empty if not found
     */
    Optional<AcademicVicePresident> findById(String id);

    /**
     * Finds an academic vice president by their name.
     * @param name the name of the academic vice president
     * @return an Optional containing the found AcademicVicePresident, or empty if not found
     */
    Optional<AcademicVicePresident> findByName(String name);

    /**
     * Finds an academic vice president by their email address.
     * @param mail the email address of the academic vice president
     * @return an Optional containing the found AcademicVicePresident, or empty if not found
     */
    Optional<AcademicVicePresident> findByMail(String mail);

    /**
     * Finds an academic vice president by their document number.
     * @param document the document number of the academic vice president
     * @return an Optional containing the found AcademicVicePresident, or empty if not found
     */
    Optional<AcademicVicePresident> findByDocument(String document);

    /**
     * Checks if an academic vice president exists by their document number.
     * @param document the document number to check
     * @return true if an academic vice president with the given document exists, false otherwise
     */
    boolean existsByDocument(String document);

    /**
     * Checks if an academic vice president exists by their email address.
     * @param mail the email address to check
     * @return true if an academic vice president with the given email exists, false otherwise
     */
    boolean existsByMail(String mail);

    /**
     * Finds academic vice presidents by their petition IDs.
     * This method helps find vice presidents who have specific petitions assigned.
     * @param petitionId the ID of the petition
     * @return a list of AcademicVicePresidents associated with the given petition
     */
    @Query("{ 'petitionIds': { $in: [?0] } }")
    List<AcademicVicePresident> findByPetitionId(String petitionId);

    /**
     * Retrieves all academic vice presidents from the database.
     * @return a list of all AcademicVicePresidents
     */
    @Override
    List<AcademicVicePresident> findAll();

    /**
     * Deletes an academic vice president by their unique identifier.
     * @param id the unique identifier of the academic vice president to delete
     */
    void deleteById(String id);

    /**
     * Counts the total number of academic vice presidents in the system
     * @return the total count of academic vice presidents
     */
    long count();
}