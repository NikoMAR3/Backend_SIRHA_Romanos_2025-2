package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionCreateDTO;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
/**
 * Interface for creating petitions of different types.
 * Each implementing class is responsible for creating a specific type of petition.
 */
public interface PetitionCreator {

    /**
     * Creates a petition entity based on the provided data transfer object.
     *
     * @param dto the data transfer object containing the petition information
     * @return the created petition entity
     */
    Petition createPetition(PetitionCreateDTO dto);

    /**
     * Calculates the priority level for the group change petition based on user-specific criteria.
     *
     * @param dto the data transfer object containing petition details
     * @return the calculated priority level as an Enumeration
     */
    default PetitionPriority calculatePriorityByUser(PetitionCreateDTO dto){
        if (studentService.searchStudentById(dto.getUserID()) != null) {
            return PetitionPriority.MEDIUM;
        } else if (deanService.searchDeanById(dto.getUserID()) != null) {
            return PetitionPriority.URGENT;
        } else {
            return PetitionPriority.LOW;
        }
    }
}
