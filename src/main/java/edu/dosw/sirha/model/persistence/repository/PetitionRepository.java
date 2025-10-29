package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.entities.PetitionPriority;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Petition entity operations.
 * Extends MongoRepository to provide basic CRUD operations and custom query methods
 * for the academic petition management system.
 */
@Repository
public interface PetitionRepository extends MongoRepository<Petition, String> {

    /**
     * Finds a petition by its unique identifier.
     *
     * @param id the petition unique identifier
     * @return an Optional containing the petition if found, empty otherwise
     */
    Optional<Petition> findById(String id);

    /**
     * Finds all petitions by their type.
     * Useful for filtering petitions by category such as GRADE_APPEAL,
     * PROGRAM_CHANGE, or ADMINISTRATIVE_REQUEST.
     *
     * @param type the petition type to filter by
     * @return a list of petitions matching the specified type
     */
    List<Petition> findByType(PetitionType type);

    /**
     * Finds all petitions by their current state.
     * Useful for filtering petitions by PENDING, IN_REVIEW, APPROVED, or REJECTED states.
     *
     * @param state the petition state to filter by
     * @return a list of petitions matching the specified state
     */
    List<Petition> findByState(PetitionState state);

    /**
     * Finds all petitions by their priority level.
     * Useful for filtering petitions by LOW, MEDIUM, HIGH, or URGENT priority.
     *
     * @param priority the petition priority to filter by
     * @return a list of petitions matching the specified priority
     */
    List<Petition> findByPriority(PetitionPriority priority);

    /**
     * Finds all petitions created by a specific deanery.
     *
     * @param deanery the name or identifier of the deanery
     * @return a list of petitions associated with the specified deanery
     */
    List<Petition> findByAssociateDeanery(String deanery);

    /**
     * Finds all petitions submitted by a specific student.
     *
     * @param studentId the unique identifier of the student
     * @return a list of petitions submitted by the specified student
     */
    List<Petition> findByStudentId(String studentId);

    /**
     * Finds all petitions related to a specific subject or course.
     *
     * @param subjectId the unique identifier of the subject
     * @return a list of petitions related to the specified subject
     */
    List<Petition> findBySubjectId(String subjectId);

    /**
     * Finds all petitions created within a specific date range.
     *
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of petitions created within the specified date range
     */
    List<Petition> findByCreationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds all petitions created on or after a specific date.
     * Useful for retrieving recent petitions.
     *
     * @param date the creation date threshold
     * @return a list of petitions created on or after the specified date
     */
    List<Petition> findByCreationDateAfter(LocalDateTime date);

    /**
     * Finds all petitions created on or before a specific date.
     *
     * @param date the creation date threshold
     * @return a list of petitions created on or before the specified date
     */
    List<Petition> findByCreationDateBefore(LocalDateTime date);

    /**
     * Finds all petitions by state and priority.
     * Useful for prioritizing petition review processes.
     *
     * @param state the petition state to filter by
     * @param priority the petition priority to filter by
     * @return a list of petitions matching both the state and priority
     */
    @Query("{'state': ?0, 'priority': ?1}")
    List<Petition> findByStateAndPriority(PetitionState state, PetitionPriority priority);

    /**
     * Finds all petitions by type and state.
     * Useful for tracking petition processing by category.
     *
     * @param type the petition type to filter by
     * @param state the petition state to filter by
     * @return a list of petitions matching both the type and state
     */
    List<Petition> findByTypeAndState(PetitionType type, PetitionState state);

    /**
     * Finds all petitions by student and state.
     * Useful for tracking a student's petition history.
     *
     * @param studentId the student unique identifier
     * @param state the petition state to filter by
     * @return a list of petitions for the student matching the specified state
     */
    List<Petition> findByStudentIdAndState(String studentId, PetitionState state);

    /**
     * Counts the number of petitions by their state.
     * Useful for generating statistical reports.
     *
     * @param state the petition state to count
     * @return the number of petitions in the specified state
     */
    long countByState(PetitionState state);

    /**
     * Counts the number of petitions by their priority.
     *
     * @param priority the petition priority to count
     * @return the number of petitions with the specified priority
     */
    long countByPriority(PetitionPriority priority);

    /**
     * Counts the number of petitions submitted by a specific student.
     *
     * @param studentId the student unique identifier
     * @return the number of petitions submitted by the student
     */
    long countByStudentId(String studentId);

    /**
     * Counts the number of petitions by their type.
     * Useful for generating statistical reports by petition category.
     *
     * @param type the petition type to count
     * @return the number of petitions with the specified type
     */
    long countByType(PetitionType type);

    /**
     * Checks if a petition exists with the given identifier.
     *
     * @param id the petition unique identifier
     * @return true if a petition exists with the given id, false otherwise
     */
    boolean existsById(String id);

    /**
     * Deletes all petitions by their state.
     * Use with caution as this operation is irreversible.
     *
     * @param state the petition state to delete
     */
    void deleteByState(PetitionState state);

    /**
     * Finds all petitions ordered by creation date in descending order.
     * Most recent petitions appear first.
     *
     * @return a list of all petitions sorted by creation date (newest first)
     */
    List<Petition> findAllByOrderByCreationDateDesc();

    /**
     * Finds all petitions by state ordered by priority in descending order.
     * Highest priority petitions appear first.
     *
     * @param state the petition state to filter by
     * @return a list of petitions in the specified state, sorted by priority (highest first)
     */
    List<Petition> findByStateOrderByPriorityDesc(PetitionState state);


    /**
     * Finds all petitions depending on the exceptional case.
     * @param isExceptionalCase boolean to look for
     * @return a list of all petitions that share the boolean.
     */
    List<Petition> findByIsExceptionalCase(Boolean isExceptionalCase);


}