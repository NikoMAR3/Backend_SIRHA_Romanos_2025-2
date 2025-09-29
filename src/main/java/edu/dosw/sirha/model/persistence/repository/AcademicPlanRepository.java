package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.AcademicPlan;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for AcademicPlan entity operations.
 * Extends MongoRepository to provide basic CRUD operations and custom query methods
 * for the collaborative task management system.
 */
@Repository
public interface AcademicPlanRepository extends MongoRepository<AcademicPlan, String> {

    /**
     * Finds an academic plan by its name.
     * @param name the name of the academic plan
     * @return an Optional containing the found AcademicPlan, or empty if not found
     */
    Optional<AcademicPlan> findByName(String name);

    /**
     * Searches for an academic plan by its unique identifier.
     * @param id the unique identifier of the academic plan
     * @return an Optional containing the found AcademicPlan, or empty if not found
     */
    Optional<AcademicPlan> searchPlanById(String id);

    /**
     * Finds academic plans associated with a specific academic program.
     * @param program the academic program to filter by
     * @return a list of AcademicPlans associated with the given program
     */
    List<AcademicPlan> findByProgram(AcademicProgram program);

    /**
     * Finds academic plans by the ID of their associated academic program.
     * @param programId the ID of the academic program
     * @return a list of AcademicPlans associated with the given program ID
     */
    List<AcademicPlan> findByProgramId(String programId);

    /**
     * Finds academic plans associated with a specific traffic light ID.
     * @param trafficLightId the ID of the traffic light to filter by
     * @return a list of AcademicPlans associated with the given traffic light ID
     */
    List<AcademicPlan> findByTrafficLightId(String trafficLightId);

    /**
     * Finds academic plans that include a specific subject by its ID.
     * @param subjectId the ID of the subject to filter by
     * @return a list of AcademicPlans that include the specified subject
     */
    @Query("{ 'subjects.$id': ?0 }")
    List<AcademicPlan> findBySubjectId(String subjectId);

    /**
     * Retrieves all academic plans from the database.
     * @return a list of all AcademicPlans
     */
    @Override
    List<AcademicPlan> findAll();
}
