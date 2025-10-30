package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.AcademicProgram;

import java.util.List;
import java.util.Optional;

import edu.dosw.sirha.model.entities.Deanery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

public interface AcademicProgramRepository extends MongoRepository<AcademicProgram,String> {

    /**
     * Finds an academic program by its name.
     * @param name the name of the academic program
     * @return an Optional containing the found AcademicProgram, or empty if not found
     */
    Optional<AcademicProgram> findByName(String name);

    /**
     * Searches for an academic program by its unique identifier.
     * @param id the unique identifier of the academic program
     * @return an Optional containing the found AcademicProgram, or empty if not found
     */
    Optional<AcademicProgram> findProgramById(String id);

    /**
     * Finds academic programs associated with a specific deanery.
     * @param deanery the deanery to filter by
     * @return a list of AcademicPrograms associated with the given deanery
     */
    List<AcademicProgram> findByDeanery(Deanery deanery);

    /**
     * Finds academic programs by the ID of their associated deanery.
     * @param deaneryId the ID of the deanery
     * @return a list of AcademicPrograms associated with the given deanery ID
     */
    List<AcademicProgram> findByDeaneryId(String deaneryId);

    /**
     * Finds academic programs that include a specific plan by its ID.
     * @param planId the ID of the academic plan to filter by
     * @return a list of AcademicPrograms that include the specified plan
     */
    @Query("{ 'plans.$id': ?0 }")
    List<AcademicProgram> findByPlanId(String planId);

    /**
     * Retrieves all academic programs from the database.
     * @return a list of all AcademicPrograms
     */
    @Override
    List<AcademicProgram> findAll();

    
}
