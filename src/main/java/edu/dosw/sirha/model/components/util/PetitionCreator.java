package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.services.AcademicVicePresidentService;
import edu.dosw.sirha.model.services.DeanService;
import edu.dosw.sirha.model.services.ProfessorService;
import edu.dosw.sirha.model.services.StudentService;
import edu.dosw.sirha.model.services.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Interface for creating petitions of different types.
 * Each implementing class is responsible for creating a specific type of petition.
 */
public abstract class PetitionCreator {
    @Autowired
    StudentService studentService;

    @Autowired
    DeanService deanService;

    @Autowired
    ProfessorService professorService;

    @Autowired
    AcademicVicePresidentService academicvicepresidentService;

    /**
     * Creates a petition entity based on the provided data transfer object.
     *
     * @param dto the data transfer object containing the petition information
     * @return the created petition entity
     */
    public abstract Petition createPetition(PetitionRequestDTO dto);

    /**
     * Determines if this creator can handle the given petition type.
     *
     * @param type the petition type to check
     * @return true if this creator can handle the type, false otherwise
     */
    public abstract boolean supports(PetitionType type);

    /**
     * Calculates the priority level for the petition based on user-specific criteria.
     *
     * @param dto the data transfer object containing petition details
     * @return the calculated priority level as an Enumeration
     */
    public PetitionPriority calculatePriorityByUser(PetitionRequestDTO dto) {
        try {
            if (studentService.searchStudentById(dto.getUserID()) != null) {
                return PetitionPriority.LOW;
            }
        } catch (RuntimeException e) {
        }

        try {
            if (deanService.searchDeanById(dto.getUserID()) != null) {
                return PetitionPriority.URGENT;
            }
        } catch (RuntimeException e) {
        }

        try {
            if (professorService.searchProfessorById(dto.getUserID()) != null) {
                return PetitionPriority.MEDIUM;
            }
        } catch (RuntimeException e) {
        }

        try {
            if (academicvicepresidentService.searchAcademicVicePresidentById(dto.getUserID()) != null) {
                return PetitionPriority.URGENT;
            }
        } catch (RuntimeException e) {
        }
        return PetitionPriority.LOW;
    }

}
