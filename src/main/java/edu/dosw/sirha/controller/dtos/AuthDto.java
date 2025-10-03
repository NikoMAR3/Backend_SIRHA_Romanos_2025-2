package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

/**
 * Class that groups all DTOs related to authentication.
 */
public class AuthDto {
    
    /**
     * DTO representing a request for user login.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request DTO for user authentication")
    public static class LoginRequest {
        
        @NotBlank(message = "Credential is required")
        @Schema(
            description = "User credential (document number or institutional email)",
            example = "12345678",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String credential;
        
        @NotBlank(message = "Password is required")
        @Schema(
            description = "User password",
            example = "mySecurePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String password;
    }
    
    /**
     * DTO representing the response of a login request.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Response DTO for login authentication")
    public static class LoginResponse {
        
        @Schema(
            description = "Indicates if the login was successful",
            example = "true"
        )
        private boolean success;
        
        @Schema(
            description = "Response message with details",
            example = "Login successful"
        )
        private String message;
        
        @Schema(
            description = "Authenticated user information",
            implementation = User.class
        )
        private User user;
    }
    
    /**
     * Generic API response DTO.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Generic API response for operations")
    public static class ApiResponse {
        
        @Schema(
            description = "Indicates if the operation was successful",
            example = "true"
        )
        private boolean success;
        
        @Schema(
            description = "Descriptive message about the operation",
            example = "Operation completed successfully"
        )
        private String message;
    }
    
    /**
     * DTO representing the response for a permission check.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Response DTO for permission verification")
    public static class PermissionResponse {
        
        @Schema(
            description = "True if the user has the required permission",
            example = "true"
        )
        private boolean hasPermission;
        
        @Schema(
            description = "The resource name or identifier being accessed",
            example = "users_manage"
        )
        private String resource;
        
        @Schema(
            description = "The user's role description",
            example = "Vicepresidente Académico"
        )
        private String userRole;
    }
    
    /**
     * DTO representing a request to set a user's password.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request DTO for setting user password")
    public static class SetPasswordRequest {
        
        @NotBlank(message = "User ID is required")
        @Schema(
            description = "The ID of the user whose password will be set",
            example = "507f1f77bcf86cd799439011",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String userId;
        
        @NotBlank(message = "New password is required")
        @Schema(
            description = "The new password to be set",
            example = "newSecurePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String newPassword;
    }
    
    /**
     * DTO representing the response with current user information.
     */
    @Data
    @NoArgsConstructor
    @Schema(description = "Response DTO with current user information")
    public static class CurrentUserResponse {
        
        @Schema(
            description = "Unique identifier of the user",
            example = "507f1f77bcf86cd799439011"
        )
        private String id;
        
        @Schema(
            description = "Full name of the user",
            example = "Juan Pérez"
        )
        private String name;
        
        @Schema(
            description = "Institutional email of the user",
            example = "juan.perez@escuelaing.edu.co"
        )
        private String mail;
        
        @Schema(
            description = "User type description",
            example = "Estudiante"
        )
        private String userType;
        
        @Schema(
            description = "Indicates if the user account is active",
            example = "true"
        )
        private boolean isActive;
        
        /**
         * Constructor that builds a response from a {@link User} entity.
         * 
         * @param user the user entity
         */
        public CurrentUserResponse(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.mail = user.getMail();
            this.userType = user.getType().getDescription();
            this.isActive = user.isActive();
        }
    }
}