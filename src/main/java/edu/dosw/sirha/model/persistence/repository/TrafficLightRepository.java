package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.TrafficLightStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link TrafficLight} entities in MongoDB.
 */
@Repository
public interface TrafficLightRepository extends MongoRepository<TrafficLight, String> {

    /**
     * Finds the traffic light record associated with a specific student.
     *
     * @param studentId the unique identifier of the student
     * @return an {@link Optional} containing the traffic light if found, otherwise empty
     */
    Optional<TrafficLight> findByStudentId(String studentId);

    /**
     * Finds all traffic light records associated with a specific student.
     *
     * @param studentId the unique identifier of the student
     * @return a list of traffic lights for the student
     */
    List<TrafficLight> findAllByStudentId(String studentId);

    /**
     * Finds all traffic light records for a given academic program.
     *
     * @param programId the identifier of the academic program
     * @return a list of traffic lights associated with the program
     */
    List<TrafficLight> findByProgramId(String programId);

    /**
     * Finds all traffic light records with a specific academic status.
     *
     * @param status the academic status (RED, GREEN, BLUE, WHITE)
     * @return a list of traffic lights matching the status
     */
    List<TrafficLight> findByStatus(TrafficLightStatus status);

    /**
     * Finds the traffic light of a student within a specific program.
     *
     * @param studentId the identifier of the student
     * @param programId the identifier of the academic program
     * @return an {@link Optional} containing the traffic light if found, otherwise empty
     */
    Optional<TrafficLight> findByStudentIdAndProgramId(String studentId, String programId);

    /**
     * Counts the number of traffic lights with a specific status.
     *
     * @param status the academic status to count by
     * @return the number of traffic lights with the given status
     */
    long countByStatus(TrafficLightStatus status);

    /**
     * Counts the number of traffic lights associated with a specific program.
     *
     * @param programId the identifier of the program
     * @return the number of traffic lights linked to the program
     */
    long countByProgramId(String programId);

    /**
     * Retrieves all traffic lights for a given semester.
     *
     * @param semester the semester number
     * @return a list of traffic lights for students in that semester
     */
    List<TrafficLight> findBySemester(int semester);

    /**
     * Finds all traffic lights where the student has an average grade higher than the given value.
     *
     * @param grade the minimum grade threshold
     * @return a list of traffic lights for students with grades above the threshold
     */
    List<TrafficLight> findByGradeGreaterThan(double grade);

    /**
     * Finds all traffic lights where the student has an average grade lower than the given value.
     *
     * @param grade the maximum grade threshold
     * @return a list of traffic lights for students with grades below the threshold
     */
    List<TrafficLight> findByGradeLessThan(double grade);
}

