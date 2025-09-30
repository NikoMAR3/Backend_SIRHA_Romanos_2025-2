package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.AcademicPlan;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.AcademicPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Academic Plans.
 * Handles business logic for academic plan operations including creation, modification,
 * deletion, and retrieval of study plans that evolve over time (ej: Plan 14, Plan 15).
 */
@Service
public class AcademicPlanService {

    
    private AcademicPlanRepository academicPlanRepository;

    public AcademicPlanService(AcademicPlanRepository academicPlanRepository) {
        this.academicPlanRepository = academicPlanRepository;
    }

    /**
     * Creates a new academic plan.
     * Validates that the plan doesn't already exist and saves it to the database.
     * 
     * @param plan the AcademicPlan to be created
     * @return the created AcademicPlan with assigned ID
     * @throws IllegalArgumentException if plan is null or already exists
     */
    public AcademicPlan createPlan(AcademicPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("El plan académico no puede ser nulo");
        }
        if (plan.getName() != null && academicPlanRepository.findByName(plan.getName()).isPresent()) {
            throw new IllegalArgumentException("El plan académico con nombre '" + plan.getName() + "' ya existe");
        }
        
        return academicPlanRepository.save(plan);
    }

    /**
     * Modifies an existing academic plan.
     * Updates the plan information while preserving the original ID.
     * 
     * @param plan the AcademicPlan with updated information
     * @return the updated AcademicPlan
     * @throws IllegalArgumentException if plan is null or doesn't exist
     */
    public AcademicPlan modifyPlan(AcademicPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("El plan académico no puede ser nulo");
        }
        if (plan.getId() == null || !academicPlanRepository.existsById(plan.getId())) {
            throw new IllegalArgumentException("El plan académico con ID '" + plan.getId() + "' no existe");
        }
        
        return academicPlanRepository.save(plan);
    }

    /**
     * Deletes an academic plan by its ID.
     * Removes the plan from the database permanently.
     * 
     * @param id the unique identifier of the academic plan to delete
     * @throws IllegalArgumentException if ID is null or plan doesn't exist
     */
    public void deletePlan(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del plan no puede ser nulo o vacío");
        }
        if (!academicPlanRepository.existsById(id)) {
            throw new IllegalArgumentException("El plan académico con ID '" + id + "' no existe");
        }
        academicPlanRepository.deleteById(id);
    }

    /**
     * Searches for an academic plan by its unique identifier.
     * 
     * @param id the unique identifier of the academic plan
     * @return the AcademicPlan if found
     * @throws IllegalArgumentException if ID is null or plan doesn't exist
     */
    public AcademicPlan searchPlanById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del plan no puede ser nulo o vacío");
        }
        
        return academicPlanRepository.findPlanById(id)
                .orElseThrow(() -> new IllegalArgumentException("El plan académico con ID '" + id + "' no fue encontrado"));
    }

    /**
     * Searches for an academic plan by its name.
     * Useful for finding specific plan versions like "Plan 14", "Plan 15", etc.
     * 
     * @param name the name of the academic plan (ej: "Plan 14", "Plan 15")
     * @return the AcademicPlan if found
     * @throws IllegalArgumentException if name is null or plan doesn't exist
     */
    public AcademicPlan searchPlanByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del plan no puede ser nulo o vacío");
        }
        
        return academicPlanRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("El plan académico con nombre '" + name + "' no fue encontrado"));
    }

    /**
     * Searches for academic plans by their associated academic program.
     * Useful for finding all study plans (ej, Plan 14, Plan 15) for a specific program.
     * 
     * @param program the AcademicProgram to search plans for
     * @return a list of AcademicPlans associated with the program
     * @throws IllegalArgumentException if program is null
     */
    public List<AcademicPlan> searchPlanByProgram(AcademicProgram program) {
        if (program == null) {
            throw new IllegalArgumentException("El programa académico no puede ser nulo");
        }
        
        return academicPlanRepository.findByProgram(program);
    }

    /**
     * Searches for academic plans by their associated academic program ID.
     * Useful for finding all study plans (e.g., Plan 14, Plan 15) for a specific program.
     * 
     * @param programId the ID of the AcademicProgram to search plans for
     * @return a list of AcademicPlans associated with the program
     * @throws IllegalArgumentException if programId is null
     */
    public List<AcademicPlan> searchPlanByProgramId(String programId) {
        if (programId == null || programId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del programa académico no puede ser nulo o vacío");
        }
        
        return academicPlanRepository.findByProgramId(programId);
    }

    /**
     * Retrieves all academic plans from the database.
     * Useful for administrative purposes and generating comprehensive reports.
     * 
     * @return a list of all AcademicPlans
     */
    public List<AcademicPlan> searchAllPlans() {
        return academicPlanRepository.findAll();
    }

}