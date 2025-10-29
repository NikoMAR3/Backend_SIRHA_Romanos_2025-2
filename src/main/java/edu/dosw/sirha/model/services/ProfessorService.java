package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing Professor entities.
 * Handles business logic for CRUD operations related to Professors, who are
 * academic staff responsible for teaching courses and conducting research.
 *
 * Professors are associated with specific deaneries (faculties) and subjects,
 * enabling the university to manage faculty assignments and teaching loads.
 */
@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    /**
     * Constructor for dependency injection of the ProfessorRepository.
     *
     * @param professorRepository the repository for Professor data access
     */
    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    /**
     * Creates a new Professor record.
     * Generates a unique UUID for the entity and populates it with data from the UserDTO.
     *
     * @param dto the UserDTO containing the professor's basic information (name, email, document)
     * @return the newly created Professor entity with assigned ID
     * @throws IllegalArgumentException if the dto is null or contains invalid data
     */
    public Professor createProfessor(UserDTO dto) {
        Professor professor = new Professor(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return professorRepository.save(professor);
    }

    public Professor save(Professor professor) {
        return professorRepository.save(professor);
    }

    /**
     * Modifies an existing Professor record.
     * Updates the professor's personal information while preserving the original ID
     * and maintaining existing relationships with deaneries and subjects.
     *
     * @param professorCode the unique identifier of the Professor to modify
     * @param dto the UserDTO containing the updated personal information
     * @return an Optional containing the updated Professor if found, or empty Optional if not found
     */
    public Optional<Professor> modifyProfessorByCode(String professorCode, UserDTO dto) {
    return professorRepository.findByProfessorCode(professorCode)
            .map(existing -> {
                existing.setName(dto.getName());
                existing.setMail(dto.getMail());
                existing.setDocument(dto.getDocument());
                return professorRepository.save(existing);
            });
}

    /**
     * Deletes a Professor record by its ID.
     * Before deletion, should ensure the professor
     *
     * @param id the unique identifier of the Professor to delete
     * @return true if the record was successfully deleted, false if no record was found
     */
    public boolean deleteProfessorByCode(String professorCode) {
        Optional<Professor> professorOpt = professorRepository.findByProfessorCode(professorCode);
        if (professorOpt.isPresent()) {
            professorRepository.delete(professorOpt.get());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Searches for a Professor by their unique professor code.
     * Useful for scenarios where professors are identified by a specific code
     * rather than their database ID, such as in course assignments or administrative tasks.
     *
     * @param professorCode the unique code of the Professor to find
     * @return the Professor entity if found, or null if no record was found
     */
    public Professor searchProfessorByCode(String professorCode) {
        return professorRepository.findByProfessorCode(professorCode).orElse(null);
    }

    /**
     * Searches for a Professor by their unique identifier.
     * Useful for retrieving individual professor details for administrative purposes,
     * profile management, or assignment verification.
     *
     * @param id the unique identifier of the Professor to find
     * @return the Professor entity if found, or null if no record was found
     */
    public Professor searchProfessorById(String id) {
        return professorRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves all Professor records from the database.
     *
     * @return a list of all Professor entities in the system
     */
    public List<Professor> searchAllProfessors() {
        return professorRepository.findAll();
    }

    /**
     * Searches for Professors associated with a specific Deanery (faculty).
     * This method supports organizational management by allowing retrieval of
     * all faculty members belonging to a particular academic unit.
     *
     * @param deanery the Deanery entity to filter professors by
     * @return a list of Professors associated with the specified Deanery
     */
    public List<Professor> searchProfessorsByDeanery(Deanery deanery) {
        return professorRepository.findByDeanery(deanery);
    }

    /**
     * Searches for Professors qualified to teach a specific Subject.
     * Essential for course assignment, scheduling, and identifying subject matter
     * experts within the university faculty.
     *
     * @param subject the Subject entity to filter professors by
     * @return a list of Professors qualified to teach the specified subject
     */
    public List<Professor> searchProfessorsBySubject(Subject subject) {
        return professorRepository.findBySubjectsContaining(subject);
    }
}