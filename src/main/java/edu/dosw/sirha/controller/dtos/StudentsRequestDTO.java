package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.AcademicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

/**
 * Data Transfer Object for student creation and update requests.
 * Contains the information required for student registration and modification.
 */
@Data
@Schema(description = "Data transfer object for student creation and update requests")
public class StudentsRequestDTO {

    /**
     * Full name of the student
     */
    @NotBlank(message = "Name is required")
    @Schema(
            description = "Full name of the student",
            example = "María Fernanda González López",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    /**
     * Institutional email address for authentication
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Schema(
            description = "Institutional email address used for authentication",
            example = "maria.gonzalez@mail.escuelaing.edu.co",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mail;

    /**
     * Unique identification document number
     */
    @NotBlank(message = "Document is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Document must contain exactly 10 digits"
    )
    @Schema(
            description = "Unique identification document number (Colombian ID - 10 digits)",
            example = "1079534086",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String document;

    /**
     * Unique student code assigned by the institution
     */
    @Schema(
            description = "Unique student code assigned by the institution",
            example = "1000123456",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String studentCode;

    /**
     * Current semester of the student
     */
    @Min(value = 1, message = "Semester must be at least 1")
    @Schema(
            description = "Current semester number of the student",
            example = "5",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer semester;

    /**
     * Academic program ID that the student is enrolled in
     */
    @Schema(
            description = "ID of the academic program the student is enrolled in",
            example = "program-engineering-systems",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String academicProgramId;

    /**
     * Deanery ID that manages the student's academic program
     */
    @Schema(
            description = "ID of the deanery that manages the student's academic program",
            example = "deanery-engineering-systems",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String deaneryId;

    /**
     * Initial academic status of the student
     */
    @Schema(
            description = "Initial academic status of the student",
            implementation = AcademicStatus.class,
            example = "ACTIVE",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private AcademicStatus academicStatus;

    /**
     * Password for institutional authentication
     */
    @Schema(
            description = "Password for institutional authentication (will be encrypted)",
            example = "Password123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String password;
}
