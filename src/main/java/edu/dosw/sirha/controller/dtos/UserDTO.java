package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for user-related operations.
 * Contains the basic information required for creating or updating users in the system.
 */
@Data
@Schema(description = "Data transfer object for user creation and modification")
public class UserDTO {

    /**
     * Full name of the user
     */
    @NotBlank(message = "Name is required")
    @Schema(
            description = "Full name of the user",
            example = "Juan Carlos Pérez García",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    /**
     * Institutional email address of the user
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Schema(
            description = "Institutional email address of the user",
            example = "juan.perez@mail.escuelaing.edu.co",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mail;

    /**
     * Unique identification document number
     */
    @NotBlank(message = "Document is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Document must contain only numbers and 10 digits"
    )
    @Schema(
            description = "Unique identification document number (10 digits)",
            example = "1000098136",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String document;

    /**
     * Type of user in the system
     */
    @NotNull(message = "User type is required")
    @Schema(
            description = "Type of user in the system",
            implementation = UserType.class,
            example = "STUDENT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UserType type;
}
