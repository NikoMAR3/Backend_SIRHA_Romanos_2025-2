package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.*;
import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;

/**
 * Utility class for authentication and authorization validation.
 * Provides helper methods to check whether a user is authenticated and whether
 * they have the required roles to access a given resource.
 */
public class AuthValidationUtils {
    
    /**
     * Validates whether the current session has an authenticated user and optionally 
     * checks if the user has one of the allowed roles.
     *
     * @param session the current HTTP session containing the authenticated user
     * @param allowedRoles optional list of roles allowed to access the resource
     * @return {@link ResponseEntity} with {@link AuthDto.ApiResponse} indicating
     *         an error (401 unauthorized or 403 forbidden) if validation fails, 
     *         or {@code null} if validation passes
     */
    public static ResponseEntity<?> validateAuthentication(HttpSession session, UserType... allowedRoles) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(new AuthDto.ApiResponse(false, "User not authenticated"));
        }
        
        if (allowedRoles.length > 0) {
            boolean hasPermission = Arrays.asList(allowedRoles).contains(user.getType());
            if (!hasPermission) {
                return ResponseEntity.status(403).body(
                    new AuthDto.ApiResponse(false, "Access denied for role: " + user.getType().getDescription())
                );
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the currently authenticated user from the session.
     *
     * @param session the current HTTP session containing the authenticated user
     * @return the {@link User} object stored in the session, or {@code null} if no user is authenticated
     */
    public static User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }
}
