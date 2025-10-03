package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.PetitionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Data Transfer Object for creating new petitions (Request).
 * Contains the information sent by the client to create a petition.
 */
@Data
@Schema(description = "Data transfer object for petition creation requests")
public class PetitionRequestDTO {

    /**
     * Unique identifier of the user creating the petition
     */
    @NotBlank(message = "User ID is required")
    @Schema(
            description = "Unique identifier of the user creating the petition",
            example = "1000098136",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userID;

    /**
     * Type of petition being created
     */
    @NotNull(message = "Petition type is required")
    @Schema(
            description = "Type of the petition",
            requiredMode = Schema.RequiredMode.REQUIRED,
            implementation = PetitionType.class,
            example = "ADD_SUBJECT"
    )
    private PetitionType type;

    /**
     * Deanery associated with the petition for automatic routing
     */
    @Schema(
            description = "Deanery for automatic routing of the petition",
            example = "Systems engineering",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String Deanery;

    /**
     * Description or justification provided by the user
     */
    @Schema(
            description = "Description or justification for the petition request",
            example = "I work full-time and need this class schedule to continue my studies",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    /**
     * Additional details specific to the petition.
     * Flexible structure that may vary depending on the petition type.
     */
    @Schema(
            description = "Map of additional details specific to the petition type. " +
                    "May contain variable information such as subjectId, associatedDeanery, classSessionId, etc.",
            example = "{\"subjectId\": \"1767\", \"groupId\": \"2\", \"associatedDeanery\": \"Systems engineering\"}",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE
    )
    private Map<String, Object> details;
}
