package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionCreateDTO;
import edu.dosw.sirha.model.entities.AcademicVicePresident;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.services.AcademicVicePresidentService;
import edu.dosw.sirha.model.services.DeanService;
import edu.dosw.sirha.model.services.ProfessorService;
import edu.dosw.sirha.model.services.StudentService;

/**
 * Interface for creating petitions of different types.
 * Each implementing class is responsible for creating a specific type of petition.
 */
public abstract class PetitionCreator {

    StudentService studentService;
    DeanService deanService;
    ProfessorService professorService ;
    AcademicVicePresidentService academicvicepresidentService;

    /**
     * Creates a petition entity based on the provided data transfer object.
     *
     * @param dto the data transfer object containing the petition information
     * @return the created petition entity
     */
    public abstract Petition createPetition(PetitionCreateDTO dto);

    /**
     * Calculates the priority level for the group change petition based on user-specific criteria.
     *
     * @param dto the data transfer object containing petition details
     * @return the calculated priority level as an Enumeration
     */
    public PetitionPriority calculatePriorityByUser(PetitionCreateDTO dto){
        if (studentService.searchStudentById(dto.getUserID()) != null) {
            return PetitionPriority.LOW;
        } else if (deanService.searchDeanById(dto.getUserID()) != null) {
            return PetitionPriority.URGENT;
        } else if (professorService.searchProfessorById(dto.getUserID()) != null) {
            return PetitionPriority.MEDIUM;
        } else if (academicvicepresidentService.searchAcademicVicePresidentById(dto.getUserID()) != null) {
            return PetitionPriority.URGENT;
        }else {
            return PetitionPriority.LOW;
        }
    }
}
