package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Schedule;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Schedule entity operations.
 * Extends MongoRepository to provide basic CRUD operations and custom query methods
 * for the academic schedule management system.
 */
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    /**
     * Finds the schedule associated with a specific student.
     * Retrieves the current or most recent schedule for the student.
     *
     * @param studentId the unique identifier of the student
     * @return an Optional containing the student's schedule if found, empty otherwise
     */
    Optional<Schedule> findByStudentId(String studentId);

    /**
     * Finds all schedules containing a specific subject by its identifier.
     * Useful for determining which students are enrolled in a course.
     *
     * @param subjectId the unique identifier of the subject
     * @return a list of schedules containing the specified subject
     */
    @Query("{'subjects.id': ?0}")
    List<Schedule> findBySubjectId(String subjectId);

    /**
     * Finds all schedules containing a subject with the specified short name.
     * Short name is typically the course code (e.g., "CS101", "MATH201").
     *
     * @param shortName the short name or code of the subject
     * @return a list of schedules containing subjects with the specified short name
     */
    @Query("{'subjects.shortName': ?0}")
    List<Schedule> findBySubjectShortName(String shortName);

    /**
     * Finds all schedules containing a subject with the specified full name.
     *
     * @param name the full name of the subject
     * @return a list of schedules containing subjects with the specified name
     */
    @Query("{'subjects.name': ?0}")
    List<Schedule> findBySubjectName(String name);

    /**
     * Finds all schedules for a specific semester.
     * Semester format typically follows "2024-1", "2024-2", etc.
     *
     * @param semester the semester identifier
     * @return a list of schedules for the specified semester
     */
    List<Schedule> findBySemester(String semester);

    /**
     * Finds all schedules for a specific academic program.
     * Program identifier may be the program name or code.
     *
     * @param program the academic program identifier
     * @return a list of schedules for the specified program
     */
    List<Schedule> findByProgram(String program);

    /**
     * Finds all schedules for a specific student and semester.
     * Useful for retrieving historical schedule data.
     *
     * @param studentId the unique identifier of the student
     * @param semester the semester identifier
     * @return a list of schedules matching both criteria
     */
    List<Schedule> findByStudentIdAndSemester(String studentId, String semester);

    /**
     * Finds the current active schedule for a specific student.
     * Assumes schedules have an "active" or "current" flag.
     *
     * @param studentId the unique identifier of the student
     * @return an Optional containing the current schedule if found, empty otherwise
     */
    @Query("{'studentId': ?0, 'active': true}")
    Optional<Schedule> findCurrentScheduleByStudentId(String studentId);

    /**
     * Finds all historical schedules for a specific student.
     * Retrieves all past schedules excluding the current one.
     *
     * @param studentId the unique identifier of the student
     * @return a list of historical schedules for the student
     */
    @Query("{'studentId': ?0, 'active': false}")
    List<Schedule> findScheduleHistoryByStudentId(String studentId);

    /**
     * Finds all schedules ordered by student ID and semester.
     * Useful for generating comprehensive schedule reports.
     *
     * @return a list of all schedules sorted by student ID and semester
     */
    List<Schedule> findAllByOrderByStudentIdAscSemesterDesc();

    /**
     * Finds all schedules for a program and semester combination.
     * Useful for program-level schedule analysis.
     *
     * @param program the academic program identifier
     * @param semester the semester identifier
     * @return a list of schedules matching both criteria
     */
    List<Schedule> findByProgramAndSemester(String program, String semester);

    /**
     * Checks if a schedule exists for a specific student.
     *
     * @param studentId the unique identifier of the student
     * @return true if a schedule exists for the student, false otherwise
     */
    boolean existsByStudentId(String studentId);

    /**
     * Checks if a schedule exists for a specific student and semester.
     *
     * @param studentId the unique identifier of the student
     * @param semester the semester identifier
     * @return true if a schedule exists for the student in the semester, false otherwise
     */
    boolean existsByStudentIdAndSemester(String studentId, String semester);

    /**
     * Counts the number of schedules for a specific semester.
     * Useful for enrollment statistics.
     *
     * @param semester the semester identifier
     * @return the number of schedules in the specified semester
     */
    long countBySemester(String semester);

    /**
     * Counts the number of schedules for a specific program.
     *
     * @param program the academic program identifier
     * @return the number of schedules in the specified program
     */
    long countByProgram(String program);

    /**
     * Counts the number of schedules containing a specific subject.
     * Useful for determining course enrollment numbers.
     *
     * @param subjectId the unique identifier of the subject
     * @return the number of schedules containing the specified subject
     */
    @Query(value = "{'subjects._id': ?0}", count = true)
    long countBySubjectId(String subjectId);

    /**
     * Deletes all schedules for a specific student.
     * Use with caution as this operation is irreversible.
     *
     * @param studentId the unique identifier of the student
     */
    void deleteByStudentId(String studentId);

    /**
     * Deletes all schedules for a specific semester.
     * Typically used for archival or cleanup operations.
     *
     * @param semester the semester identifier
     */
    void deleteBySemester(String semester);

    /**
     * Finds schedules by program ordered by semester in descending order.
     * Most recent semesters appear first.
     *
     * @param program the academic program identifier
     * @return a list of schedules for the program sorted by semester (newest first)
     */
    List<Schedule> findByProgramOrderBySemesterDesc(String program);

}