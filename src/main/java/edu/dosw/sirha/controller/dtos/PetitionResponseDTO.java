package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for petition responses.
 * Contains the complete information of a petition returned to the client.
 */
@Data
@Schema(description = "Data transfer object for petition responses")
public class PetitionResponseDTO {

    /**
     * Unique identifier of the petition
     */
    @Schema(
            description = "Unique identifier of the petition",
            example = "12345"
    )
    private String petitionId;

    /**
     * Unique identifier of the student who created the petition
     */
    @Schema(
            description = "Unique identifier of the student who created the petition",
            example = "1000098136"
    )
    private String studentId;

    /**
     * Type of petition
     */
    @Schema(
            description = "Type or category of the petition",
            implementation = PetitionType.class,
            example = "ADD_SUBJECT"
    )
    private PetitionType type;

    /**
     * Subject identifier related to the petition
     */
    @Schema(
            description = "Subject identifier related to the petition",
            example = "1767"
    )
    private String subjectId;

    /**
     * Short name of the subject
     */
    @Schema(
            description = "Short name of the subject",
            example = "DOSW"
    )
    private String subjectShortName;

    /**
     * Full name of the subject
     */
    @Schema(
            description = "Full name of the subject",
            example = "Desarrollo de operaciones Software"
    )
    private String subjectName;

    /**
     * Associate deanery handling the petition
     */
    @Schema(
            description = "Associate deanery responsible for handling the petition",
            example = "Systems engineering"
    )
    private String associateDeanery;

    /**
     * Priority level of the petition (assigned automatically)
     */
    @Schema(
            description = "Priority level assigned to the petition based on user type and arrival order",
            implementation = PetitionPriority.class,
            example = "LOW"
    )
    private PetitionPriority priority;

    /**
     * Current state of the petition
     */
    @Schema(
            description = "Current processing state of the petition",
            implementation = PetitionState.class,
            example = "PENDING"
    )
    private PetitionState state;

    /**
     * Date and time when the petition was created
     */
    @Schema(
            description = "Timestamp when the petition was created",
            example = "2025-01-08T10:30:00"
    )
    private LocalDateTime creationDate;

    /**
     * Date and time when the petition was last modified
     */
    @Schema(
            description = "Timestamp when the petition was last modified",
            example = "2025-01-08T14:45:00"
    )
    private LocalDateTime modificationDate;

    /**
     * Description or justification provided by the user
     */
    @Schema(
            description = "Description or justification provided with the petition",
            example = "I work full-time and need this class schedule"
    )
    private String justification;

    /**
     * ID of the reviewer assigned to handle this petition
     */
    @Schema(
            description = "Identifier of the reviewer assigned to handle this petition",
            example = "1000098136"
    )
    private String assignedReviewer;

    /**
     * Reason for rejection if the petition was denied
     */
    @Schema(
            description = "Reason provided if the petition was rejected",
            example = "Subject capacity reached"
    )
    private String rejectionReason;

    /**
     * History of all decisions made on this petition (for traceability)
     */
    @Schema(
            description = "Complete history of decisions and status changes for traceability",
            example = "[\"2025-01-08: Created by 1000098136\", \"2025-01-09: Assigned to reviewer-456\", \"2025-01-10: Approved\"]"
    )
    private List<String> decisionHistory;

    /**
     * Position in queue for automatic priority assignment
     */
    @Schema(
            description = "Position in the processing queue based on arrival order and priority",
            example = "15"
    )
    private Integer queuePosition;
}
