package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
/**
 * Component responsible for creating ADD_SUBJECT type petitions.
 * Implements the PetitionCreator interface to handle the specific business logic
 * required for subject addition petition creation.
 *
 * This creator is used when students need to request adding a new subject
 * to their academic curriculum.
 */


@Component
public class AddSubjectCreator extends PetitionCreator {

    /**
     * Creates a new ADD_SUBJECT petition based on the provided DTO.
     * Initializes a petition entity with specific fields required for
     * subject addition requests, including subject identification and
     * automatic priority calculation.
     *
     * @param dto the data transfer object containing petition creation information
     * @return a fully initialized Petition entity for subject addition
     */

    @Override
    public Petition createPetition(PetitionRequestDTO dto) {
        Petition petition = new Petition();
        petition.setPetitionId(UUID.randomUUID().toString());
        petition.setStudentId(dto.getUserID());
        petition.setType(PetitionType.ADD_SUBJECT);
        petition.setSubjectId((String) dto.getDetails().get("subjectId"));
        petition.setJustification(dto.getDescription());
        petition.setPriority(calculatePriorityByUser(dto));
        petition.setState(PetitionState.PENDING);
        petition.setCreationDate(LocalDateTime.now());
        return petition;
    }

    @Override
    public boolean supports(PetitionType type) {
        return type == PetitionType.ADD_SUBJECT;
    }
}

