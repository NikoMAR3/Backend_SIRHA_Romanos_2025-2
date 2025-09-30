package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.AcademicPlan;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.persistence.repository.AcademicProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Academic Programs (university careers).
 * Handles business logic for academic program operations including creation, modification,
 * deletion, and retrieval of university careers such as Computer Engineering, Civil Engineering, etc.
 */
@Service
public class AcademicProgramService {

    private AcademicProgramRepository academicProgramRepository;

    public AcademicProgramService(AcademicProgramRepository academicProgramRepository) {
        this.academicProgramRepository = academicProgramRepository;
    }

    /**
     * Creates a new academic program (university career).
     * Validates that the program doesn't already exist and saves it to the database.
     * 
     * @param program the AcademicProgram to be created
     * @return the created AcademicProgram with assigned ID
     * @throws IllegalArgumentException if program is null or already exists
     */
    public AcademicProgram createProgram(AcademicProgram program) {
        if (program == null) {
            throw new IllegalArgumentException("El programa académico no puede ser nulo");
        }
        if (program.getName() != null && academicProgramRepository.findByName(program.getName()).isPresent()) {
            throw new IllegalArgumentException("El programa académico con nombre '" + program.getName() + "' ya existe");
        }
        
        return academicProgramRepository.save(program);
    }

    /**
     * Modifies an existing academic program.
     * Updates the program information while preserving the original ID.
     * 
     * @param program the AcademicProgram with updated information
     * @return the updated AcademicProgram
     * @throws IllegalArgumentException if program is null or doesn't exist
     */
    public AcademicProgram modifyProgram(AcademicProgram program) {
        if (program == null) {
            throw new IllegalArgumentException("El programa académico no puede ser nulo");
        }
        if (program.getId() == null || !academicProgramRepository.existsById(program.getId())) {
            throw new IllegalArgumentException("El programa académico con ID '" + program.getId() + "' no existe");
        }
        
        return academicProgramRepository.save(program);
    }

    /**
     * Deletes an academic program by its ID.
     * Validates that the program exists and has no associated academic plans before deletion.
     * 
     * @param id the unique identifier of the academic program to delete
     * @throws IllegalArgumentException if ID is null, program doesn't exist, or has associated plans
     */
    public void deleteProgram(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del programa no puede ser nulo o vacío");
        }
        
        Optional<AcademicProgram> programOpt = academicProgramRepository.findById(id);
        if (programOpt.isEmpty()) {
            throw new IllegalArgumentException("El programa académico con ID '" + id + "' no existe");
        }
        
        AcademicProgram program = programOpt.get();
        if (program.getPlans() != null && !program.getPlans().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el programa '" + program.getName() + 
                                             "' porque tiene " + program.getPlans().size() + " planes académicos asociados");
        }
        
        academicProgramRepository.deleteById(id);
    }

    /**
     * Searches for an academic program by its unique identifier.
     * 
     * @param id the unique identifier of the academic program
     * @return the AcademicProgram if found
     * @throws IllegalArgumentException if ID is null or program doesn't exist
     */
    public AcademicProgram searchProgramById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del programa no puede ser nulo o vacío");
        }
        
        return academicProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El programa académico con ID '" + id + "' no fue encontrado"));
    }


    /**
     * Retrieves all academic programs from the database.
     * Useful for administrative purposes and generating comprehensive reports.
     * 
     * @return a list of all AcademicPrograms
     */
    public List<AcademicProgram> searchAllPrograms() {
        return academicProgramRepository.findAll();
    }


    /**
     * Searches for an academic program by its name.
     * Useful for finding specific careers like "Ingeniería de Sistemas", "Ingeniería Civil", etc.
     * 
     * @param name the name of the academic program
     * @return the AcademicProgram if found
     * @throws IllegalArgumentException if name is null or program doesn't exist
     */
    public AcademicProgram searchProgramByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del programa no puede ser nulo o vacío");
        }
        
        return academicProgramRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("El programa académico con nombre '" + name + "' no fue encontrado"));
    }

    //REVISAR
    /**
     * Counts the total number of academic plans in a program.
     * 
     * @param programId the ID of the academic program
     * @return the number of academic plans in the program
     */
    public int countPlansInProgram(String programId) {
        AcademicProgram program = searchProgramById(programId);
        return program.getPlans() != null ? program.getPlans().size() : 0;
    }

    //REVISAR
    /**
     * Gets the current active academic plan for a program.
     * Assumes the last plan in the list is the current one.
     * 
     * @param programId the ID of the academic program
     * @return the current AcademicPlan if exists
     */
    public Optional<AcademicPlan> getCurrentPlanForProgram(String programId) {
        AcademicProgram program = searchProgramById(programId);
        
        if (program.getPlans() == null || program.getPlans().isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(program.getPlans().get(program.getPlans().size() - 1));
    }

}