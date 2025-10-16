package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for authentication and user-related authorization logic.
 * Handles user login, password management, role validation, permission checks,
 * and administrative user operations.
 */
@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor for {@link AuthenticationService}.
     *
     * @param userRepository repository to manage user persistence
     * @param passwordEncoder encoder to securely hash and verify passwords
     */
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Authenticates a user with institutional credentials (document or email).
     *
     * @param credential document number or email
     * @param password plain-text password provided by the user
     * @return {@link AuthenticationResult} indicating success or failure and the authenticated user if successful
     * @throws IllegalArgumentException if credential or password is null
     */
    public AuthenticationResult authenticate(String id, String password) {
        if (id == null || password == null) {
            throw new IllegalArgumentException("Credentials cannot be null");
        }

        Optional<User> userOpt = userRepository.findByIdAndIsActiveTrue(id);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByMailAndIsActiveTrue(id);
        }
        
        if (userOpt.isEmpty()) {
            return new AuthenticationResult(false, "User not found or inactive", null);
        }
        
        User user = userOpt.get();
        
        if (user.getPasswordHash() == null) {
            return new AuthenticationResult(false, "User has no configured password", null);
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return new AuthenticationResult(false, "Incorrect password", null);
        }
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        return new AuthenticationResult(true, "Authentication successful", user);
    }
    
    /**
     * Sets a password for a user (admin only).
     *
     * @param userId the ID of the target user
     * @param newPassword the new password to set
     * @param adminRole the role of the user performing the operation
     * @return the updated {@link User} with the new password
     * @throws IllegalArgumentException if the role is not admin or the user is not found
     */
    public User setUserPassword(String userId, String newPassword, UserType adminRole) {
        if (adminRole != UserType.ACADEMIC_VICEPRESIDENT) {
            throw new IllegalArgumentException("Only the administrator can set passwords");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    /**
     * Checks if a user has a specific role.
     *
     * @param userId the ID of the user
     * @param requiredRole the required role to verify
     * @return true if the user has the required role, false otherwise
     */
    public boolean hasRole(String userId, UserType requiredRole) {
        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().getType() == requiredRole;
    }
    
    /**
     * Checks if a role can access a given resource.
     *
     * @param userRole the role of the user
     * @param resource the resource name or identifier
     * @return true if the role can access the resource, false otherwise
     */
    public boolean canAccessResource(UserType userRole, String resource) {
        return switch (resource) {
            case "admin_panel" -> userRole == UserType.ACADEMIC_VICEPRESIDENT;
            case "academic_programs" -> userRole == UserType.ACADEMIC_VICEPRESIDENT || userRole == UserType.DEAN;
            case "class_sessions_manage" -> userRole == UserType.ACADEMIC_VICEPRESIDENT || userRole == UserType.DEAN || userRole == UserType.PROFESSOR;
            case "class_sessions_view" -> true; // Everyone can view
            case "enrollments_manage" -> userRole != UserType.STUDENT; // Everyone except students
            case "enrollments_view" -> true; // Everyone can view their own enrollments
            case "users_manage" -> userRole == UserType.ACADEMIC_VICEPRESIDENT;
            default -> false;
        };
    }
    
    /**
     * Checks if the current user can access another user's data.
     *
     * @param currentUser the user performing the action
     * @param targetUserId the ID of the target user
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessUserData(User currentUser, String targetUserId) {
        if (currentUser.getType() == UserType.ACADEMIC_VICEPRESIDENT) {
            return true;
        }
        
        if (currentUser.getId().equals(targetUserId)) {
            return true;
        }
        
        if (currentUser.getType() == UserType.DEAN) {
            Optional<User> targetUser = userRepository.findById(targetUserId);
            return targetUser.isPresent() && 
                   (targetUser.get().getType() == UserType.STUDENT || targetUser.get().getType() == UserType.PROFESSOR);
        }
        
        return false;
    }

    /**
     * Retrieves all active users (admin only).
     *
     * @return list of active {@link User}s
     */
    public List<User> getAllUsers() {
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Retrieves users by role.
     *
     * @param type the {@link UserType} to filter by
     * @return list of users matching the role
     */
    public List<User> getUsersByType(UserType type) {
        return userRepository.findByType(type);
    }

    /**
     * Toggles a user's active status (enabled/disabled).
     *
     * @param userId the ID of the user
     * @return the updated {@link User} with toggled status
     * @throws IllegalArgumentException if user is not found
     */
    public User toggleUserStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    /**
     * Searches users by name.
     *
     * @param name the name or partial name to search for
     * @return list of users whose names contain the given value
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Checks if a user exists by document.
     *
     * @param document the document identifier
     * @return true if a user with the document exists, false otherwise
     */
    public boolean userExistsByDocument(String document) {
        return userRepository.existsByDocument(document);
    }

    /**
     * Checks if a user exists by email.
     *
     * @param email the email address
     * @return true if a user with the email exists, false otherwise
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByMail(email);
    }

    /**
     * DTO class representing the result of an authentication attempt.
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        /**
         * Constructor for {@link AuthenticationResult}.
         *
         * @param success true if authentication succeeded
         * @param message descriptive message about the result
         * @param user the authenticated user, or null if authentication failed
         */
        public AuthenticationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        /**
         * @return true if authentication succeeded, false otherwise
         */
        public boolean isSuccess() { return success; }
        
        /**
         * @return message describing the authentication result
         */
        public String getMessage() { return message; }
        
        /**
         * @return the authenticated user, or null if authentication failed
         */
        public User getUser() { return user; }
    }

    public User registerUser(User user, String plainPassword) {
    user.setPasswordHash(passwordEncoder.encode(plainPassword));
    user.setActive(true);
    user.setLastLogin(null);
    return userRepository.save(user);
    }
}
