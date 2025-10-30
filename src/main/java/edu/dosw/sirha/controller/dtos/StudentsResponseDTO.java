package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.AcademicStatus;
import edu.dosw.sirha.model.entities.TrafficLightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for student responses.
 * Contains comprehensive student information including academic status and petition history.
 */
@Data
@Schema(description = "Data transfer object for student responses with complete academic information")
public class StudentsResponseDTO {

    /**
     * Full name of the student
     */
    @Schema(
            description = "Full name of the student",
            example = "María Fernanda González López"
    )
    private String name;

    /**
     * Institutional email address
     */
    @Schema(
            description = "Institutional email address used for authentication",
            example = "maria.gonzalez@mail.escuelaing.edu.co"
    )
    private String mail;

    /**
     * Identification document number
     */
    @Schema(
            description = "Unique identification document number",
            example = "1079534086"
    )
    private String document;

    /**
     * Unique student code
     */
    @Schema(
            description = "Unique student code assigned by the institution",
            example = "1000098136"
    )
    private String studentCode;

    /**
     * Current semester of the student
     */
    @Schema(
            description = "Current semester number of the student",
            example = "5"
    )
    private Integer semester;

    /**
     * Academic program information
     */
    @Schema(
            description = "Information about the student's academic program",
            example = "{\"id\": \"program-engineering-systems\", \"name\": \"Systems Engineering\", \"code\": \"INGSIST\"}"
    )
    private Map<String, Object> academicProgram;

    /**
     * Deanery information
     */
    @Schema(
            description = "Information about the deanery managing the student",
            example = "{\"id\": \"deanery-engineering-systems\", \"name\": \"Systems Engineering Deanery\"}"
    )
    private Map<String, Object> deanery;

    /**
     * Current academic status
     */
    @Schema(
            description = "Current academic status of the student",
            implementation = AcademicStatus.class,
            example = "ACTIVE"
    )
    private AcademicStatus academicStatus;

    /**
     * Academic traffic light status and information
     */
    @Schema(
            description = "Academic traffic light information (green=normal, blue=in_progress, red=failed)",
            example = "{\"status\": \"GREEN\", \"gpa\": 4.2, \"creditsCompleted\": 85, \"totalCredits\": 144}"
    )
    private Map<String, Object> trafficLight;

    /**
     * Current semester schedule
     */
    @Schema(
            description = "Current semester schedule with enrolled subjects and class times",
            example = "[{\"subjectId\": \"1234\", \"subjectName\": \"Desarrollo de operaciones software\", \"group\": \"2\", \"schedule\": \"MWF 08:00-10:00\"}]"
    )
    private List<Map<String, Object>> currentSchedule;

    /**
     * Authentication status
     */
    @Schema(
            description = "Authentication status and last login information",
            example = "{\"isAuthenticated\": true, \"lastLogin\": \"2025-01-08T09:15:00\", \"loginAttempts\": 0}"
    )
    private Map<String, Object> authenticationInfo;

    /**
     * Registration date in the system
     */
    @Schema(
            description = "Date when the student was registered in the system",
            example = "2023-02-15T08:00:00"
    )
    private LocalDateTime registrationDate;

    /**
     * Last update date
     */
    @Schema(
            description = "Date of the last update to student information",
            example = "2025-01-08T10:30:00"
    )
    private LocalDateTime lastUpdateDate;
}
