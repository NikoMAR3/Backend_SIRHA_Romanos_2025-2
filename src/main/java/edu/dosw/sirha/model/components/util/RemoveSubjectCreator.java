package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionCreateDTO;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Component responsible for creating REMOVE_SUBJECT type petitions.
 * Implements the PetitionCreator interface to handle the specific business logic
 * required for subject removal petition creation.
 *
 * This creator is used when students need to request removing a subject
 * from their academic curriculum or course load.
 */

@Component
public class RemoveSubjectCreator implements PetitionCreator {

    /**
     * Creates a new REMOVE_SUBJECT petition based on the provided DTO.
     * Initializes a petition entity with specific fields required for
     * subject removal requests, including subject identification and
     * automatic priority calculation based on user type.
     *
     * @param dto the data transfer object containing petition creation information
     * @return a fully initialized Petition entity for subject removal
     */
    @Override
    public Petition createPetition(PetitionCreateDTO dto) {
        Petition petition = new Petition();
        petition.setPetitionId(UUID.randomUUID().toString());
        petition.setStudentId(dto.getUserID());
        petition.setType(PetitionType.REMOVE_SUBJECT);
        petition.setSubjectId((String) dto.getDetails().get("subjectId"));
        petition.setPriority(calculatePriorityByUser(dto));
        petition.setState(PetitionState.PENDING);
        petition.setCreationDate(LocalDateTime.now());
        return petition;
    }
}

