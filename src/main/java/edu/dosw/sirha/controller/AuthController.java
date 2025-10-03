package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.AuthDto;
import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.services.AuthenticationService;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * REST controller for handling authentication and authorization operations.
 * Provides endpoints for login, logout, user session management, permission checks,
 * password management, and administrative user management.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    /**
     * Constructor to inject {@link AuthenticationService}.
     *
     * @param authenticationService the service that handles authentication and authorization logic
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * User login endpoint.
     *
     * @param request the login request containing credential and password
     * @param session the current HTTP session where user data will be stored
     * @return {@link ResponseEntity} containing a {@link AuthDto.LoginResponse} with login result
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request, HttpSession session) {
        try {
            AuthenticationService.AuthenticationResult result = 
                authenticationService.authenticate(request.getCredential(), request.getPassword());
            
            if (result.isSuccess()) {
                // Store user data in session
                session.setAttribute("user", result.getUser());
                session.setAttribute("userId", result.getUser().getId());
                session.setAttribute("userType", result.getUser().getType());
                
                return ResponseEntity.ok(new AuthDto.LoginResponse(true, "Login successful", result.getUser()));
            } else {
                return ResponseEntity.badRequest().body(new AuthDto.LoginResponse(false, result.getMessage(), null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.LoginResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * User logout endpoint.
     *
     * @param session the current HTTP session that will be invalidated
     * @return {@link ResponseEntity} containing a {@link AuthDto.ApiResponse} indicating success
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthDto.ApiResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Logout successful"));
    }
    
    /**
     * Retrieve the currently authenticated user.
     *
     * @param session the current HTTP session used to validate authentication
     * @return {@link ResponseEntity} containing {@link AuthDto.CurrentUserResponse} with user info
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) return authCheck;
        
        User user = AuthValidationUtils.getCurrentUser(session);
        return ResponseEntity.ok(new AuthDto.CurrentUserResponse(user));
    }
    
    /**
     * Check if the current user has permission to access a resource.
     *
     * @param resource the resource identifier
     * @param session the current HTTP session used to validate authentication
     * @return {@link ResponseEntity} containing {@link AuthDto.PermissionResponse} with permission result
     */
    @GetMapping("/check-permission/{resource}")
    public ResponseEntity<?> checkPermission(@PathVariable String resource, HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) return authCheck;
        
        User user = AuthValidationUtils.getCurrentUser(session);
        boolean hasPermission = authenticationService.canAccessResource(user.getType(), resource);
        
        return ResponseEntity.ok(new AuthDto.PermissionResponse(hasPermission, resource, user.getType().getDescription()));
    }
    
    /**
     * Set a password for a user (admin only).
     *
     * @param request the password set request containing userId and newPassword
     * @param session the current HTTP session used to validate admin privileges
     * @return {@link ResponseEntity} containing {@link AuthDto.ApiResponse} indicating success or failure
     */
    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestBody AuthDto.SetPasswordRequest request, HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) return authCheck;
        
        try {
            User currentUser = AuthValidationUtils.getCurrentUser(session);
            authenticationService.setUserPassword(request.getUserId(), request.getNewPassword(), currentUser.getType());
            
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Password successfully set"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }
    
    /**
     * Retrieve all users (admin only).
     *
     * @param session the current HTTP session used to validate admin privileges
     * @return {@link ResponseEntity} containing the list of all users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) return authCheck;
        
        try {
            return ResponseEntity.ok(authenticationService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }
    
    /**
     * Toggle user status (activate/deactivate) (admin only).
     *
     * @param userId the ID of the user whose status will be toggled
     * @param session the current HTTP session used to validate admin privileges
     * @return {@link ResponseEntity} containing {@link AuthDto.ApiResponse} with result message
     */
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId, HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) return authCheck;
        
        try {
            User updatedUser = authenticationService.toggleUserStatus(userId);
            String status = updatedUser.isActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "User " + status + " successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }
}
