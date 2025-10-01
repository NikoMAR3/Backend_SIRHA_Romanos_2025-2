package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.PetitionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * Data Transfer Object for creating new petitions.
 * Contains the basic information required to create a petition in the system.
 */
@Data
@Schema(description = "Data transfer object for petition creation")
public class PetitionCreateDTO {

    /**
     * Unique identifier of the user creating the petition
     */
    @Schema(
            description = "Unique identifier of the user creating the petition",
            example = "user-12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userID;

    /**
     * Type of petition being created
     */
    @Schema(
            description = "Type or category of the petition",
            requiredMode = Schema.RequiredMode.REQUIRED,
            implementation = PetitionType.class
    )
    private PetitionType type;

    /**
     * Additional details specific to the petition.
     * Flexible structure that may vary depending on the petition type.
     */
    @Schema(
            description = "Map of additional details specific to the petition type. " +
                    "May contain variable information such as description, associatedDeanery, category, etc.",
            example = "{\"description\": \"I WORK, NEED THIS CLASS\", \"associatedDeanery\": \"COMPUTER_SCIENCE\"}",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE
    )
    private Map<String, Object> details;
}
