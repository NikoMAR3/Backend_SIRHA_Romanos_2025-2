package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.AuthDto;
import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.services.AuthenticationService;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * REST controller for handling authentication and authorization operations.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication Management", description = "Endpoints for user authentication, authorization and session management")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationService authenticationService;
    
    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * User login endpoint.
     */
    @PostMapping("/login")
    @Operation(summary = "User authentication", description = "Authenticates a user with institutional credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials or user not found")
    })
    public ResponseEntity<AuthDto.LoginResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request, 
            HttpSession session) {
        
        logger.info("Login attempt for credential: {}", request.getCredential());
        
        AuthenticationService.AuthenticationResult result = 
            authenticationService.authenticate(request.getCredential(), request.getPassword());
        
        if (result.isSuccess()) {
            if (!result.getUser().isActive()) {
                logger.warn("Login attempt by inactive user: {}", result.getUser().getId());
                throw new IllegalArgumentException("User account is deactivated");
            }

            session.setAttribute("user", result.getUser());
            session.setAttribute("userId", result.getUser().getId());
            session.setAttribute("userType", result.getUser().getType());
            
            logger.info("Login successful for user: {} ({}) - Role: {}", 
                       result.getUser().getName(), 
                       result.getUser().getId(),
                       result.getUser().getType().getDescription());
            
            return ResponseEntity.ok(new AuthDto.LoginResponse(true, "Login successful", result.getUser()));
        } else {
            logger.warn("Login failed for credential: {} - {}", request.getCredential(), result.getMessage());
        
            throw new IllegalArgumentException("Invalid credentials: " + result.getMessage());
        }
    }
    
    /**
     * User logout endpoint.
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the current user session")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<AuthDto.ApiResponse> logout(HttpSession session) {
        
        User currentUser = AuthValidationUtils.getCurrentUser(session);
        if (currentUser != null) {
            logger.info("Logout for user: {} ({})", currentUser.getName(), currentUser.getType().getDescription());
        }
        
        session.invalidate();
        return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Logout successful"));
    }
    
    /**
     * Retrieve the currently authenticated user.
     */
    @GetMapping("/current-user")
    @Operation(summary = "Get current user", description = "Retrieves current authenticated user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current user information retrieved"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        
        logger.debug("Retrieving current user information");
        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) return authCheck;
        
        User user = AuthValidationUtils.getCurrentUser(session);
        logger.debug("Current user: {} with role: {}", user.getId(), user.getType().getDescription());

        return ResponseEntity.ok(new AuthDto.CurrentUserResponse(user));
    }
    
    /**
     * Check if the current user has permission to access a resource.
     */
    @GetMapping("/check-permission/{resource}")
    @Operation(summary = "Check resource permissions", description = "Verifies user permissions for a resource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission check completed"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> checkPermission(
            @Parameter(description = "Resource identifier", required = true)
            @PathVariable String resource, 
            HttpSession session) {
        
        logger.debug("Checking permission for resource: {}", resource);
        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) return authCheck;
        
        User user = AuthValidationUtils.getCurrentUser(session);
        boolean hasPermission = authenticationService.canAccessResource(user.getType(), resource);

        logger.debug("Permission check for user {} on resource {}: {}", 
                    user.getId(), resource, hasPermission);
        
        return ResponseEntity.ok(new AuthDto.PermissionResponse(hasPermission, resource, user.getType().getDescription()));
    }
    
    /**
     * Set a password for a user (admin only).
     */
    @PostMapping("/set-password")
    @Operation(summary = "Set user password", description = "Sets a new password for a user (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password set successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<AuthDto.ApiResponse> setPassword(
            @Valid @RequestBody AuthDto.SetPasswordRequest request, 
            HttpSession session) {
        
        logger.info("Password set request for user: {}", request.getUserId());
        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) return (ResponseEntity<AuthDto.ApiResponse>) authCheck;
        
        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (currentUser.getId().equals(request.getUserId())) {
            logger.warn("User {} attempted to set password for themselves", currentUser.getId());
            return ResponseEntity.badRequest()
                .body(new AuthDto.ApiResponse(false, "Cannot set password for your own account"));
        }
        
        authenticationService.setUserPassword(request.getUserId(), request.getNewPassword(), currentUser.getType());
        
        logger.info("Password set successfully for user: {} by admin: {}", 
                   request.getUserId(), currentUser.getName());
        
        return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Password successfully set"));
    }
    
    /**
     * Retrieve all users (admin only).
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves all users (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users list retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        
        logger.debug("Retrieving all users");
        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) return authCheck;
        
        User currentUser = AuthValidationUtils.getCurrentUser(session);
        logger.info("User {} requesting all users list", currentUser.getId());
        

        List<User> users = authenticationService.getAllUsers();
        logger.info("Retrieved {} users", users.size());

        return ResponseEntity.ok(users);
    }
    
    /**
     * Toggle user status (activate/deactivate) - ACADEMIC_VICEPRESIDENT only.
     */
    @PutMapping("/users/{userId}/toggle-status")
    @Operation(summary = "Toggle user status", description = "Activates or deactivates a user account (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or cannot deactivate own account"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only Academic Vice President can change user status"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<AuthDto.ApiResponse> toggleUserStatus(
            @Parameter(description = "ID of the user to toggle status", required = true)
            @PathVariable String userId, 
            HttpSession session) {
        
        logger.info("Toggle status request for user: {}", userId);
        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) return (ResponseEntity<AuthDto.ApiResponse>) authCheck;
        
        User currentUser = AuthValidationUtils.getCurrentUser(session);
        
        if (currentUser.getId().equals(userId)) {
            logger.warn("User {} attempted to toggle their own status", currentUser.getId());
            return ResponseEntity.badRequest()
                .body(new AuthDto.ApiResponse(false, "Cannot change status of your own account"));
        }
        
        User updatedUser = authenticationService.toggleUserStatus(userId);
        String status = updatedUser.isActive() ? "activated" : "deactivated";
        
        logger.info("User {} ({}) successfully {} by admin: {}", 
                   updatedUser.getName(), updatedUser.getId(), status, currentUser.getName());
        
        return ResponseEntity.ok(new AuthDto.ApiResponse(true, 
            String.format("User %s successfully %s", updatedUser.getName(), status)));
    }
}