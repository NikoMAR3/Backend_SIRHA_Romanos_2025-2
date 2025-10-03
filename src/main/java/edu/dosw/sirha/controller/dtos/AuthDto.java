package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.User;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that groups all DTOs related to authentication.
 */
public class AuthDto {
    
    /**
     * DTO representing a request for user login.
     */
    @Getter
    @Setter
    public static class LoginRequest {
        private String credential;
        private String password;
        
        /**
         * Default constructor.
         */
        public LoginRequest() {}
        
        /**
         * Constructor with all parameters.
         * 
         * @param credential the username, email, or document used to authenticate
         * @param password the user's password
         */
        public LoginRequest(String credential, String password) {
            this.credential = credential;
            this.password = password;
        }
        
        /**
         * @return the credential provided by the user
         */
        public String getCredential() { return credential; }
        
        /**
         * @param credential sets the user credential
         */
        public void setCredential(String credential) { this.credential = credential; }
        
        /**
         * @return the user's password
         */
        public String getPassword() { return password; }
        
        /**
         * @param password sets the user's password
         */
        public void setPassword(String password) { this.password = password; }
    }
    
    /**
     * DTO representing the response of a login request.
     */
    @Getter
    @Setter
    public static class LoginResponse {
        private boolean success;
        private String message;
        private User user;
        
        /**
         * Default constructor.
         */
        public LoginResponse() {}
        
        /**
         * Constructor with all parameters.
         * 
         * @param success indicates if the login was successful
         * @param message response message with details
         * @param user the authenticated user entity
         */
        public LoginResponse(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        /**
         * @return true if the login was successful, false otherwise
         */
        public boolean isSuccess() { return success; }
        
        /**
         * @param success sets the login success flag
         */
        public void setSuccess(boolean success) { this.success = success; }
        
        /**
         * @return message providing information about the login attempt
         */
        public String getMessage() { return message; }
        
        /**
         * @param message sets the response message
         */
        public void setMessage(String message) { this.message = message; }
        
        /**
         * @return the authenticated user
         */
        public User getUser() { return user; }
        
        /**
         * @param user sets the authenticated user
         */
        public void setUser(User user) { this.user = user; }
    }
    
    /**
     * Generic API response DTO.
     */
    @Getter
    @Setter
    public static class ApiResponse {
        private boolean success;
        private String message;
        
        /**
         * Default constructor.
         */
        public ApiResponse() {}
        
        /**
         * Constructor with all parameters.
         * 
         * @param success indicates if the API operation was successful
         * @param message descriptive message about the operation
         */
        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        /**
         * @return true if the operation succeeded, false otherwise
         */
        public boolean isSuccess() { return success; }
        
        /**
         * @param success sets the operation success flag
         */
        public void setSuccess(boolean success) { this.success = success; }
        
        /**
         * @return message with additional operation details
         */
        public String getMessage() { return message; }
        
        /**
         * @param message sets the response message
         */
        public void setMessage(String message) { this.message = message; }
    }
    
    /**
     * DTO representing the response for a permission check.
     */
    @Getter
    @Setter
    public static class PermissionResponse {
        private boolean hasPermission;
        private String resource;
        private String userRole;
        
        /**
         * Default constructor.
         */
        public PermissionResponse() {}
        
        /**
         * Constructor with all parameters.
         * 
         * @param hasPermission true if the user has permission
         * @param resource the resource being accessed
         * @param userRole the role of the user
         */
        public PermissionResponse(boolean hasPermission, String resource, String userRole) {
            this.hasPermission = hasPermission;
            this.resource = resource;
            this.userRole = userRole;
        }
        
        /**
         * @return true if the user has the required permission
         */
        public boolean isHasPermission() { return hasPermission; }
        
        /**
         * @param hasPermission sets whether the user has permission
         */
        public void setHasPermission(boolean hasPermission) { this.hasPermission = hasPermission; }
        
        /**
         * @return the resource name or identifier
         */
        public String getResource() { return resource; }
        
        /**
         * @param resource sets the resource name
         */
        public void setResource(String resource) { this.resource = resource; }
        
        /**
         * @return the user's role
         */
        public String getUserRole() { return userRole; }
        
        /**
         * @param userRole sets the user's role
         */
        public void setUserRole(String userRole) { this.userRole = userRole; }
    }
    
    /**
     * DTO representing a request to set a user's password.
     */
    @Getter
    @Setter
    public static class SetPasswordRequest {
        private String userId;
        private String newPassword;
        
        /**
         * Default constructor.
         */
        public SetPasswordRequest() {}
        
        /**
         * Constructor with all parameters.
         * 
         * @param userId the ID of the user
         * @param newPassword the new password to be set
         */
        public SetPasswordRequest(String userId, String newPassword) {
            this.userId = userId;
            this.newPassword = newPassword;
        }
        
        /**
         * @return the user ID
         */
        public String getUserId() { return userId; }
        
        /**
         * @param userId sets the user ID
         */
        public void setUserId(String userId) { this.userId = userId; }
        
        /**
         * @return the new password
         */
        public String getNewPassword() { return newPassword; }
        
        /**
         * @param newPassword sets the new password
         */
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * DTO representing the response with current user information.
     */
    @Getter
    @Setter
    public static class CurrentUserResponse {
        private String id;
        private String name;
        private String mail;
        private String userType;
        private boolean isActive;
        
        /**
         * Default constructor.
         */
        public CurrentUserResponse() {}
        
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
