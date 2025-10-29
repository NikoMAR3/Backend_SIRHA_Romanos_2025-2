package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Component responsible for creating CHANGE_GROUP type petitions.
 * Implements the PetitionCreator interface to handle the specific business logic
 * required for group change petition creation.
 *
 * This creator is used when students need to request changing their assigned group
 * for a specific subject, including transferring to a different associate deanery.
 */


@Component
public class ChangeGroupCreator extends PetitionCreator {

    /**
     * Creates a new CHANGE_GROUP petition based on the provided DTO.
     * Extracts specific details from the DTO and sets up the petition entity
     * with appropriate values for a group change request.
     *
     * @param  dto the data transfer object containing petition creation information
     * @return a fully initialized Petition entity ready for persistence
     */

    @Override
    public Petition createPetition(PetitionRequestDTO dto) {
        Petition petition = new Petition();
        petition.setPetitionId(UUID.randomUUID().toString());
        petition.setStudentId(dto.getUserID());
        petition.setType(PetitionType.CHANGE_GROUP);
        petition.setSubjectId((String) dto.getDetails().get("subjectId"));
        petition.setJustification(dto.getDescription());
        petition.setPriority(calculatePriorityByUser(dto));
        petition.setState(PetitionState.PENDING);
        petition.setCreationDate(LocalDateTime.now());
        return petition;
    }

    @Override
    public boolean supports(PetitionType type) {
        return type == PetitionType.CHANGE_GROUP;
    }
}
