package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Subject;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Professor entity operations.
 * Extends MongoRepository to provide basic CRUD operations and custom query methods
 * for the academic professor management system.
 */
@Repository
public interface ProfessorRepository extends MongoRepository<Professor, String> {

    /**
     * Finds a professor by their unique identifier.
     *
     * @param id the professor unique identifier
     * @return an Optional containing the professor if found, empty otherwise
     */
    Optional<Professor> findById(String id);

    /**
     * Finds all professors associated with a specific deanery.
     * Useful for retrieving faculty members by academic department or faculty.
     *
     * @param deanery the deanery entity to filter by
     * @return a list of professors belonging to the specified deanery
     */
    List<Professor> findByDeanery(Deanery deanery);

    /**
     * Finds all professors by deanery name.
     * Alternative method using deanery identifier instead of entity.
     *
     * @param deaneryName the name of the deanery
     * @return a list of professors belonging to the specified deanery
     */
    @Query("{'deanery.name': ?0}")
    List<Professor> findByDeaneryName(String deaneryName);

    /**
     * Finds all professors teaching a specific subject.
     * Useful for identifying faculty members by course assignment.
     *
     * @param subject the subject entity to filter by
     * @return a list of professors teaching the specified subject
     */
    List<Professor> findBySubjectsContaining(Subject subject);

    /**
     * Finds all professors by subject identifier.
     * Alternative method using subject ID for lookup.
     *
     * @param subjectId the unique identifier of the subject
     * @return a list of professors teaching the subject with the given ID
     */
    @Query("{'subjects._id': ?0}")
    List<Professor> findBySubjectId(String subjectId);

    /**
     * Finds all professors by subject name.
     * Useful for searching professors by course name.
     *
     * @param subjectName the name of the subject
     * @return a list of professors teaching subjects matching the given name
     */
    @Query("{'subjects.name': ?0}")
    List<Professor> findBySubjectName(String subjectName);

    /**
     * Finds a professor by their email address.
     * Email is typically unique in the system.
     *
     * @param email the professor's email address
     * @return an Optional containing the professor if found, empty otherwise
     */
    Optional<Professor> findByMail(String email);

    /**
     * Finds a professor by their document identifier.
     * Document number is typically unique (ID card, passport, etc.).
     *
     * @param document the professor's document identifier
     * @return an Optional containing the professor if found, empty otherwise
     */
    Optional<Professor> findByDocument(String document);

    /**
     * Finds all professors whose name contains the specified string (case-insensitive).
     * Useful for search functionality.
     *
     * @param name the name or partial name to search for
     * @return a list of professors whose name contains the search term
     */
    List<Professor> findByNameContainingIgnoreCase(String name);

    /**
     * Finds all professors by deanery and subject.
     * Useful for finding faculty teaching specific courses in a department.
     *
     * @param deanery the deanery entity to filter by
     * @param subject the subject entity to filter by
     * @return a list of professors matching both criteria
     */
    @Query("{'deanery': ?0, 'subjects': ?1}")
    List<Professor> findByDeaneryAndSubject(Deanery deanery, Subject subject);

    /**
     * Counts the number of professors in a specific deanery.
     * Useful for generating statistical reports by department.
     *
     * @param deanery the deanery entity to count professors for
     * @return the number of professors in the specified deanery
     */
    long countByDeanery(Deanery deanery);

    /**
     * Counts the number of professors teaching a specific subject.
     *
     * @param subject the subject entity to count professors for
     * @return the number of professors teaching the specified subject
     */
    long countBySubjectsContaining(Subject subject);

    /**
     * Checks if a professor exists with the given email address.
     * Useful for validation during registration.
     *
     * @param email the email address to check
     * @return true if a professor exists with the given email, false otherwise
     */
    boolean existsByMail(String email);

    /**
     * Checks if a professor exists with the given document identifier.
     *
     * @param document the document identifier to check
     * @return true if a professor exists with the given document, false otherwise
     */
    boolean existsByDocument(String document);

    /**
     * Deletes all professors associated with a specific deanery.
     * Use with caution as this operation is irreversible.
     *
     * @param deanery the deanery entity whose professors will be deleted
     */
    void deleteByDeanery(Deanery deanery);

    /**
     * Finds all professors ordered by name in ascending order.
     *
     * @return a list of all professors sorted alphabetically by name
     */
    List<Professor> findAllByOrderByNameAsc();

    /**
     * Finds professors by deanery ordered by name.
     *
     * @param deanery the deanery to filter by
     * @return a list of professors in the specified deanery, sorted by name
     */
    List<Professor> findByDeaneryOrderByNameAsc(Deanery deanery);
}