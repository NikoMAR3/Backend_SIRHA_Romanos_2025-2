package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ClassSession entity operations.
 * Extends MongoRepository to provide basic CRUD operations and custom query methods
 */
@Repository
public interface ClassSessionRepository extends MongoRepository<ClassSession, String> {

    /**
     * Finds a class session by its unique identifier.
     * @param id the unique identifier of the class session
     * @return an Optional containing the found ClassSession, or empty if not found
     */
    Optional<ClassSession> findById(String id);

    /**
     * Finds class sessions by professor ID.
     * @param professorId the ID of the professor
     * @return a list of ClassSessions assigned to the given professor
     */
    List<ClassSession> findByProfessorId(String professorId);

    /**
     * Finds class sessions by subject short name.
     * @param subjectShortName the short name of the subject
     * @return a list of ClassSessions for the given subject
     */
    List<ClassSession> findBySubjectShortName(String subjectShortName);

    /**
     * Checks if there are scheduling conflicts for a given session ID.
     * This method would typically check for time/classroom conflicts.
     * @param id the ID of the session to check for conflicts
     * @return true if there are conflicts, false otherwise
     */
    @Query("{ $and: [ { '_id': { $ne: ?0 } }, { $or: [ { 'dayOfWeek': ?1, 'startTime': { $lt: ?3 }, 'endTime': { $gt: ?2 }, 'classroom': ?4 }, { 'professorId': ?5, 'dayOfWeek': ?1, 'startTime': { $lt: ?3 }, 'endTime': { $gt: ?2 } } ] } ] }")
    boolean existsScheduleConflicts(String id, String dayOfWeek, String startTime, String endTime, String classroom, String professorId);

    /**
     * Finds all class sessions with available capacity (enrolled < capacity).
     * @return a list of ClassSessions that have available spots
     */
    @Query("{ 'enrolledStudents': { $lt: '$capacity' } }")
    List<ClassSession> findSessionsWithAvailableCapacity();

    /**
     * Finds class sessions by subject name (full name, not short name).
     * @param subjectName the full name of the subject
     * @return a list of ClassSessions for the given subject name
     */
    List<ClassSession> findBySubjectName(String subjectName);

    /**
     * Retrieves all class sessions from the database.
     * @return a list of all ClassSessions
     */
    @Override
    List<ClassSession> findAll();

    /**
     * Finds all class sessions where a specific student is enrolled.
     * @param studentId the ID of the student
     * @return a list of ClassSessions where the student is enrolled
     */
    @Query("{ 'enrolledStudentIds': ?0 }")
    List<ClassSession> findByEnrolledStudentId(String studentId);

    /**
     * Finds all class sessions where a specific student is on the waiting list.
     * @param studentId the ID of the student
     * @return a list of ClassSessions where the student is on waiting list
     */
    @Query("{ 'waitingListStudentIds': ?0 }")
    List<ClassSession> findByWaitingListStudentId(String studentId);

    /**
     * Checks if a student is already enrolled in a specific session.
     * @param id the session ID
     * @param studentId the student ID
     * @return true if student is enrolled, false otherwise
     */
    @Query("{ '_id': ?0, 'enrolledStudentIds': ?1 }")
    boolean existsByIdAndEnrolledStudentId(String id, String studentId);

    /**
     * Finds sessions by subject short name and available capacity.
     * @param subjectShortName the short name of the subject
     * @return list of sessions with available spots
     */
    @Query("{ 'subjectShortName': ?0, 'enrolledStudents': { $lt: '$capacity' } }")
    List<ClassSession> findBySubjectShortNameWithAvailableCapacity(String subjectShortName);

}