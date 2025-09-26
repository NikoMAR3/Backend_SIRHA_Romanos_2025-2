package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.ClassSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSessionRepository extends MongoRepository<ClassSession, String> {
    /**
     * Find class sessions by subject ID
     */
    List<ClassSession> findBySubjectId(String subjectId);

    /**
     * Find class sessions by professor
     */
    List<ClassSession> findByProfessor(String professor);

    /**
     * Find class sessions by day
     */
    List<ClassSession> findByDay(String day);

    /**
     * Find class sessions by state
     */
    List<ClassSession> findByState(String state);

    /**
     * Find class sessions by class number
     */
    Optional<ClassSession> findByClassNum(String classNum);

    /**
     * Find class sessions by subject ID and class number
     */
    Optional<ClassSession> findBySubjectIdAndClassNum(String subjectId, String classNum);

    /**
     * Find class sessions that have available quota
     */
    @Query("{'currentQuota': {$lt: '$maxQuota'}, 'state': 'ACTIVE'}")
    List<ClassSession> findAvailableClassSessions();

    /**
     * Find class sessions with quota warning (90% or more)
     */
    @Query("{'$expr': {'$gte': [{'$divide': ['$currentQuota', '$maxQuota']}, 0.9]}, 'state': 'ACTIVE'}")
    List<ClassSession> findClassSessionsWithQuotaWarning();

    /**
     * Find full class sessions
     */
    @Query("{'$expr': {'$eq': ['$currentQuota', '$maxQuota']}}")
    List<ClassSession> findFullClassSessions();

    /**
     * Find class sessions by classroom
     */
    List<ClassSession> findByClassroom(String classroom);

    /**
     * Find class sessions that contain a specific student
     */
    List<ClassSession> findByStudentsIdContaining(String studentId);

    /**
     * Find active class sessions by subject
     */
    List<ClassSession> findBySubjectIdAndState(String subjectId, String state);

    /**
     * Find class sessions by day and time range
     */
    List<ClassSession> findByDayAndStartTimeBetween(String day, String startTime, String endTime);
}
