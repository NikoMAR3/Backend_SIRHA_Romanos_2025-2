package edu.dosw.sirha.controller.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for manager request operations.
 * Contains information required for deanery management operations.
 */
@Data
@Schema(description = "Data transfer object for manager requests in deanery management")
public class ManagerRequestDTO {

    /**
     * ID of the petition to process
     */
    @Schema(
            description = "Unique identifier of the petition",
            example = "petition-123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String petitionId;

    /**
     * ID of the student for queries
     */
    @Schema(
            description = "Unique identifier of the student",
            example = "1000098136",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String studentId;

    /**
     * ID of the class session to query
     */
    @Schema(
            description = "Unique identifier of the class session",
            example = "session-789",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String sessionId;

    /**
     * Response decision for petition processing
     */
    @Schema(
            description = "Manager's decision on the petition",
            allowableValues = {"APPROVED", "REJECTED", "MORE_INFO_REQUIRED"},
            example = "APPROVED"
    )
    private String decision;

    /**
     * Reason for rejection or additional information request
     */
    @Schema(
            description = "Justification for the decision made",
            example = "The requested group has no available capacity"
    )
    private String justification;

    /**
     * Start date for change period configuration
     */
    @Schema(
            description = "Start date for the change period",
            example = "2024-01-15T08:00:00"
    )
    private LocalDateTime periodStartDate;

    /**
     * End date for change period configuration
     */
    @Schema(
            description = "End date for the change period",
            example = "2024-01-31T23:59:59"
    )
    private LocalDateTime periodEndDate;

    /**
     * Subject short name for alternative group search
     */
    @Schema(
            description = "Short name of the subject to search for alternatives",
            example = "CALI"
    )
    private String subjectShortName;

    /**
     * Capacity threshold for monitoring alerts
     */
    @Min(value = 1, message = "Capacity threshold must be at least 1")
    @Schema(
            description = "Capacity threshold percentage for alerts (1-100)",
            example = "90",
            minimum = "1",
            maximum = "100"
    )
    private Integer capacityThreshold;

    /**
     * Filter by petition state
     */
    @Schema(
            description = "Filter petitions by state",
            allowableValues = {"PENDING", "IN_REVIEW", "APPROVED", "REJECTED"},
            example = "PENDING"
    )
    private String petitionState;

    /**
     * Manager's deanery ID for access control
     */
    @NotBlank(message = "Deanery ID is required")
    @Schema(
            description = "ID of the deanery the manager belongs to",
            example = "deanery-systems-engineering",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String deaneryId;

    /**
     * List of session IDs for bulk operations
     */
    @Schema(
            description = "List of session IDs for bulk monitoring or operations"
    )
    private List<String> sessionIds;
}
