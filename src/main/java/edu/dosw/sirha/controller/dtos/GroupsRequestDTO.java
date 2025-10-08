package edu.dosw.sirha.controller.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for creating new groups (Request).
 * Contains the information sent by the client to create a group.
 */
@Data
@Schema(description = "Data transfer object for group creation requests")
public class GroupsRequestDTO {

    /**
     * Name of the group
     */
    @NotBlank(message = "Group name is required")
    @Schema(
            description = "Name of the group",
            example = "Grupo A - Programación Orientada a Objetos",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String groupName;

    /**
     * Description of the group
     */
    @Schema(
            description = "Description or additional information about the group",
            example = "Grupo de trabajo para el proyecto final de POO",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    /**
     * ID of the professor assigned to this group
     */
    @NotBlank(message = "Professor ID is required")
    @Schema(
            description = "Unique identifier of the professor assigned to the group",
            example = "prof123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String professorId;

    /**
     * ID of the subject this group belongs to
     */
    @NotBlank(message = "Subject ID is required")
    @Schema(
            description = "Unique identifier of the subject this group belongs to",
            example = "subj456",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String subjectId;

    /**
     * ID of the class session this group is associated with
     */
    @Schema(
            description = "Unique identifier of the class session associated with the group",
            example = "session789",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String classSessionId;

    /**
     * Maximum number of students allowed in this group
     */
    @Min(value = 1, message = "Maximum students must be at least 1")
    @Max(value = 100, message = "Maximum students cannot exceed 100")
    @Schema(
            description = "Maximum number of students allowed in the group",
            example = "30",
            minimum = "1",
            maximum = "100",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer maxStudents;

    /**
     * List of student IDs to be initially enrolled in the group
     */
    @Schema(
            description = "List of student IDs to be initially enrolled in the group",
            example = "[\"student123\", \"student456\", \"student789\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> studentIds;

    /**
     * List of schedule IDs associated with this group
     */
    @Schema(
            description = "List of schedule IDs that define when the group meets",
            example = "[\"schedule123\", \"schedule456\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> scheduleIds;

    /**
     * List of class schedule IDs for specific time slots
     */
    @Schema(
            description = "List of class schedule IDs defining specific time slots for the group",
            example = "[\"classSchedule123\", \"classSchedule456\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> classScheduleIds;

    /**
     * Additional details specific to the group.
     * Flexible structure that may vary depending on the group type or requirements.
     */
    @Schema(
            description = "Map of additional details specific to the group. " +
                    "May contain variable information such as classroom, semester, groupType, etc.",
            example = "{\"classroom\": \"Lab A-301\", \"semester\": \"2025-1\", \"groupType\": \"LABORATORY\"}",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE
    )
    private Map<String, Object> details;
}