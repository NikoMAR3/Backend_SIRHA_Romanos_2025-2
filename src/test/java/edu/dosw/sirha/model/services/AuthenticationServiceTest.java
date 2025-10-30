package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.persistence.repository.UserRepository;
import edu.dosw.sirha.model.services.AuthenticationService.AuthenticationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 * Validates all authentication, authorization, and user management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private User adminUser;
    private User studentUser;
    private User professorUser;
    private User deanUser;

    /**
     * Sets up test data before each test execution.
     * Creates sample users with different roles and configurations using concrete User subclasses.
     */
    @BeforeEach
    void setUp() {
        // Usar Student para el usuario principal de test
        testUser = new Student("user123", "Juan Pérez", "juan.perez@escuelaing.edu.co", "12345678");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setActive(true);

        // Usar AcademicVicePresident para admin
        adminUser = new AcademicVicePresident("admin123", "María Admin", "maria.admin@escuelaing.edu.co", "87654321");
        adminUser.setPasswordHash("$2a$10$adminHashedPassword");
        adminUser.setActive(true);

        // Usar Student para otro estudiante
        studentUser = new Student("student123", "Carlos Estudiante", "carlos.estudiante@escuelaing.edu.co", "11111111");
        studentUser.setActive(true);

        // Usar Professor para profesor
        professorUser = new Professor("prof123", "Ana Profesora", "ana.profesora@escuelaing.edu.co", "22222222");
        professorUser.setActive(true);

        // Usar Dean para decano
        deanUser = new Dean("dean123", "Luis Decano", "luis.decano@escuelaing.edu.co", "33333333");
        deanUser.setActive(true);
    }

    @Nested
    @DisplayName("authenticate() Tests")
    class AuthenticateTests {

        /**
         * Tests successful authentication with document credential.
         */
        @Test
        @DisplayName("Should authenticate successfully with valid document and password")
        void authenticate_ValidDocumentAndPassword_ShouldReturnSuccessResult() {
            when(userRepository.findByIdAndIsActiveTrue("12345678"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            AuthenticationResult result = authenticationService.authenticate("12345678", "password123");

            assertTrue(result.isSuccess());
            assertEquals("Authentication successful", result.getMessage());
            assertNotNull(result.getUser());
            assertEquals(testUser.getId(), result.getUser().getId());
            verify(userRepository).save(testUser);
        }

        /**
         * Tests successful authentication with email credential.
         */
        @Test
        @DisplayName("Should authenticate successfully with valid email and password")
        void authenticate_ValidEmailAndPassword_ShouldReturnSuccessResult() {
            when(userRepository.findByIdAndIsActiveTrue("juan.perez@escuelaing.edu.co")).thenReturn(Optional.empty());
            when(userRepository.findByMailAndIsActiveTrue("juan.perez@escuelaing.edu.co")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            AuthenticationResult result = authenticationService.authenticate("juan.perez@escuelaing.edu.co", "password123");

            assertTrue(result.isSuccess());
            assertEquals("Authentication successful", result.getMessage());
            assertNotNull(result.getUser());
            assertEquals(testUser.getId(), result.getUser().getId());
            verify(userRepository).save(testUser);
        }

        /**
         * Tests authentication failure when user is not found.
         */
        @Test
        @DisplayName("Should fail authentication when user is not found")
        void authenticate_UserNotFound_ShouldReturnFailureResult() {
            when(userRepository.findByIdAndIsActiveTrue("nonexistent")).thenReturn(Optional.empty());
            when(userRepository.findByMailAndIsActiveTrue("nonexistent")).thenReturn(Optional.empty());

            AuthenticationResult result = authenticationService.authenticate("nonexistent", "password123");

            assertFalse(result.isSuccess());
            assertEquals("User not found or inactive", result.getMessage());
            assertNull(result.getUser());
            verify(userRepository, never()).save(any());
        }

        /**
         * Tests authentication failure when user has no password configured.
         */
        @Test
        @DisplayName("Should fail authentication when user has no password")
        void authenticate_UserWithoutPassword_ShouldReturnFailureResult() {
            Student userWithoutPassword = new Student("user456", "Usuario Sin Password", "sin.password@escuelaing.edu.co", "99999999");
            userWithoutPassword.setPasswordHash(null);

            when(userRepository.findByIdAndIsActiveTrue("99999999")).thenReturn(Optional.of(userWithoutPassword));
            AuthenticationResult result = authenticationService.authenticate("99999999", "password123");

            assertFalse(result.isSuccess());
            assertEquals("User has no configured password", result.getMessage());
            assertNull(result.getUser());
            verify(userRepository, never()).save(any());
        }

        /**
         * Tests authentication failure with incorrect password.
         */
        @Test
        @DisplayName("Should fail authentication with incorrect password")
        void authenticate_IncorrectPassword_ShouldReturnFailureResult() {
            when(userRepository.findByIdAndIsActiveTrue("12345678"))
                    .thenReturn(Optional.of(testUser)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);

            AuthenticationResult result = authenticationService.authenticate("12345678", "wrongpassword");

            assertFalse(result.isSuccess());
            assertEquals("Incorrect password", result.getMessage());
            assertNull(result.getUser());
            verify(userRepository, never()).save(any());
        }

        /**
         * Tests exception handling when credential is null.
         */
        @Test
        @DisplayName("Should throw exception when credential is null")
        void authenticate_NullCredential_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.authenticate(null, "password123")
            );

            assertEquals("Credentials cannot be null", exception.getMessage());
            verify(userRepository, never()).findByDocumentAndIsActiveTrue(anyString());
            verify(userRepository, never()).findByMailAndIsActiveTrue(anyString());
        }

        /**
         * Tests exception handling when password is null.
         */
        @Test
        @DisplayName("Should throw exception when password is null")
        void authenticate_NullPassword_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.authenticate("12345678", null)
            );

            assertEquals("Credentials cannot be null", exception.getMessage());
            verify(userRepository, never()).findByDocumentAndIsActiveTrue(anyString());
            verify(userRepository, never()).findByMailAndIsActiveTrue(anyString());
        }

        /**
         * Tests that last login time is updated on successful authentication.
         */
        @Test
        @DisplayName("Should update last login time on successful authentication")
        void authenticate_SuccessfulLogin_ShouldUpdateLastLoginTime() {
            LocalDateTime beforeAuth = LocalDateTime.now();
            
            when(userRepository.findByIdAndIsActiveTrue("12345678")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            authenticationService.authenticate("12345678", "password123");

            assertNotNull(testUser.getLastLogin());
            assertTrue(testUser.getLastLogin().isAfter(beforeAuth) || testUser.getLastLogin().isEqual(beforeAuth));
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("setUserPassword() Tests")
    class SetUserPasswordTests {

        /**
         * Tests successful password setting by admin.
         */
        @Test
        @DisplayName("Should set password successfully when admin role is provided")
        void setUserPassword_ValidAdminRole_ShouldSetPassword() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newHashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = authenticationService.setUserPassword("user123", "newPassword123", UserType.ACADEMIC_VICEPRESIDENT);

            assertNotNull(result);
            assertEquals("$2a$10$newHashedPassword", testUser.getPasswordHash());
            verify(userRepository).findById("user123");
            verify(passwordEncoder).encode("newPassword123");
            verify(userRepository).save(testUser);
        }

        /**
         * Tests exception when non-admin tries to set password.
         */
        @Test
        @DisplayName("Should throw exception when non-admin tries to set password")
        void setUserPassword_NonAdminRole_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.setUserPassword("user123", "newPassword123", UserType.STUDENT)
            );

            assertEquals("Only the administrator can set passwords", exception.getMessage());
            verify(userRepository, never()).findById(anyString());
            verify(userRepository, never()).save(any());
        }

        /**
         * Tests exception when user is not found.
         */
        @Test
        @DisplayName("Should throw exception when user is not found")
        void setUserPassword_UserNotFound_ShouldThrowException() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.setUserPassword("nonexistent", "newPassword123", UserType.ACADEMIC_VICEPRESIDENT)
            );

            assertEquals("User not found", exception.getMessage());
            verify(userRepository).findById("nonexistent");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("hasRole() Tests")
    class HasRoleTests {

        /**
         * Tests role check when user has the required role.
         */
        @Test
        @DisplayName("Should return true when user has required role")
        void hasRole_UserHasRole_ShouldReturnTrue() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

            boolean result = authenticationService.hasRole("user123", UserType.STUDENT);

            assertTrue(result);
            verify(userRepository).findById("user123");
        }

        /**
         * Tests role check when user does not have the required role.
         */
        @Test
        @DisplayName("Should return false when user does not have required role")
        void hasRole_UserDoesNotHaveRole_ShouldReturnFalse() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

            boolean result = authenticationService.hasRole("user123", UserType.PROFESSOR);

            assertFalse(result);
            verify(userRepository).findById("user123");
        }

        /**
         * Tests role check when user is not found.
         */
        @Test
        @DisplayName("Should return false when user is not found")
        void hasRole_UserNotFound_ShouldReturnFalse() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            boolean result = authenticationService.hasRole("nonexistent", UserType.STUDENT);

            assertFalse(result);
            verify(userRepository).findById("nonexistent");
        }
    }

    @Nested
    @DisplayName("canAccessResource() Tests")
    class CanAccessResourceTests {

        /**
         * Tests admin panel access permissions.
         */
        @Test
        @DisplayName("Should allow only Academic Vice President to access admin panel")
        void canAccessResource_AdminPanel_ShouldAllowOnlyAdmin() {
            assertTrue(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "admin_panel"));
            assertFalse(authenticationService.canAccessResource(UserType.DEAN, "admin_panel"));
            assertFalse(authenticationService.canAccessResource(UserType.PROFESSOR, "admin_panel"));
            assertFalse(authenticationService.canAccessResource(UserType.STUDENT, "admin_panel"));
        }

        /**
         * Tests academic programs access permissions.
         */
        @Test
        @DisplayName("Should allow Academic Vice President and Dean to access academic programs")
        void canAccessResource_AcademicPrograms_ShouldAllowAdminAndDean() {
            assertTrue(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "academic_programs"));
            assertTrue(authenticationService.canAccessResource(UserType.DEAN, "academic_programs"));
            assertFalse(authenticationService.canAccessResource(UserType.PROFESSOR, "academic_programs"));
            assertFalse(authenticationService.canAccessResource(UserType.STUDENT, "academic_programs"));
        }

        /**
         * Tests class sessions management permissions.
         */
        @Test
        @DisplayName("Should allow Admin, Dean, and Professor to manage class sessions")
        void canAccessResource_ClassSessionsManage_ShouldAllowAdminDeanProfessor() {
            assertTrue(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "class_sessions_manage"));
            assertTrue(authenticationService.canAccessResource(UserType.DEAN, "class_sessions_manage"));
            assertTrue(authenticationService.canAccessResource(UserType.PROFESSOR, "class_sessions_manage"));
            assertFalse(authenticationService.canAccessResource(UserType.STUDENT, "class_sessions_manage"));
        }

        /**
         * Tests class sessions view permissions.
         */
        @Test
        @DisplayName("Should allow everyone to view class sessions")
        void canAccessResource_ClassSessionsView_ShouldAllowEveryone() {
            assertTrue(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "class_sessions_view"));
            assertTrue(authenticationService.canAccessResource(UserType.DEAN, "class_sessions_view"));
            assertTrue(authenticationService.canAccessResource(UserType.PROFESSOR, "class_sessions_view"));
            assertTrue(authenticationService.canAccessResource(UserType.STUDENT, "class_sessions_view"));
        }

        /**
         * Tests enrollments management permissions.
         */
        @Test
        @DisplayName("Should allow everyone except students to manage enrollments")
        void canAccessResource_EnrollmentsManage_ShouldAllowEveryoneExceptStudents() {
            assertTrue(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "enrollments_manage"));
            assertTrue(authenticationService.canAccessResource(UserType.DEAN, "enrollments_manage"));
            assertTrue(authenticationService.canAccessResource(UserType.PROFESSOR, "enrollments_manage"));
            assertFalse(authenticationService.canAccessResource(UserType.STUDENT, "enrollments_manage"));
        }

        /**
         * Tests users management permissions.
         */
        @Test
        @DisplayName("Should allow only Academic Vice President to manage users")
        void canAccessResource_UsersManage_ShouldAllowOnlyAdmin() {
            assertTrue(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "users_manage"));
            assertFalse(authenticationService.canAccessResource(UserType.DEAN, "users_manage"));
            assertFalse(authenticationService.canAccessResource(UserType.PROFESSOR, "users_manage"));
            assertFalse(authenticationService.canAccessResource(UserType.STUDENT, "users_manage"));
        }

        /**
         * Tests unknown resource access.
         */
        @Test
        @DisplayName("Should deny access to unknown resources")
        void canAccessResource_UnknownResource_ShouldDenyAccess() {
            assertFalse(authenticationService.canAccessResource(UserType.ACADEMIC_VICEPRESIDENT, "unknown_resource"));
            assertFalse(authenticationService.canAccessResource(UserType.DEAN, "unknown_resource"));
            assertFalse(authenticationService.canAccessResource(UserType.PROFESSOR, "unknown_resource"));
            assertFalse(authenticationService.canAccessResource(UserType.STUDENT, "unknown_resource"));
        }
    }

    @Nested
    @DisplayName("canAccessUserData() Tests")
    class CanAccessUserDataTests {

        /**
         * Tests that admin can access any user data.
         */
        @Test
        @DisplayName("Should allow Academic Vice President to access any user data")
        void canAccessUserData_AdminUser_ShouldAllowAccessToAnyUser() {
            boolean result = authenticationService.canAccessUserData(adminUser, "anyUserId");

            assertTrue(result);
        }

        /**
         * Tests that users can access their own data.
         */
        @Test
        @DisplayName("Should allow users to access their own data")
        void canAccessUserData_SameUser_ShouldAllowAccess() {
            boolean result = authenticationService.canAccessUserData(testUser, "user123");

            assertTrue(result);
        }

        /**
         * Tests that dean can access student and professor data.
         */
        @Test
        @DisplayName("Should allow Dean to access student and professor data")
        void canAccessUserData_DeanToStudentOrProfessor_ShouldAllowAccess() {
            when(userRepository.findById("student123")).thenReturn(Optional.of(studentUser));
            when(userRepository.findById("prof123")).thenReturn(Optional.of(professorUser));

            assertTrue(authenticationService.canAccessUserData(deanUser, "student123"));
            assertTrue(authenticationService.canAccessUserData(deanUser, "prof123"));
            
            verify(userRepository).findById("student123");
            verify(userRepository).findById("prof123");
        }

        /**
         * Tests that dean cannot access admin data.
         */
        @Test
        @DisplayName("Should not allow Dean to access admin data")
        void canAccessUserData_DeanToAdmin_ShouldDenyAccess() {
            when(userRepository.findById("admin123")).thenReturn(Optional.of(adminUser));

            boolean result = authenticationService.canAccessUserData(deanUser, "admin123");

            assertFalse(result);
            verify(userRepository).findById("admin123");
        }

        /**
         * Tests that students cannot access other users' data.
         */
        @Test
        @DisplayName("Should not allow students to access other users data")
        void canAccessUserData_StudentToOtherUser_ShouldDenyAccess() {
            boolean result = authenticationService.canAccessUserData(studentUser, "otherUserId");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getAllUsers() Tests")
    class GetAllUsersTests {

        /**
         * Tests retrieval of all active users.
         */
        @Test
        @DisplayName("Should return all active users")
        void getAllUsers_ShouldReturnActiveUsers() {
            List<User> expectedUsers = Arrays.asList(testUser, adminUser, studentUser);
            when(userRepository.findByIsActiveTrue()).thenReturn(expectedUsers);

            List<User> result = authenticationService.getAllUsers();

            assertNotNull(result);
            assertEquals(3, result.size());
            assertTrue(result.contains(testUser));
            assertTrue(result.contains(adminUser));
            assertTrue(result.contains(studentUser));
            verify(userRepository).findByIsActiveTrue();
        }

        /**
         * Tests retrieval when no users exist.
         */
        @Test
        @DisplayName("Should return empty list when no active users exist")
        void getAllUsers_NoActiveUsers_ShouldReturnEmptyList() {
            when(userRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

            List<User> result = authenticationService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("getUsersByType() Tests")
    class GetUsersByTypeTests {

        /**
         * Tests retrieval of users by specific type.
         */
        @Test
        @DisplayName("Should return users of specified type")
        void getUsersByType_ValidType_ShouldReturnUsersOfType() {
            List<User> expectedStudents = Arrays.asList(testUser, studentUser);
            when(userRepository.findByType(UserType.STUDENT)).thenReturn(expectedStudents);

            List<User> result = authenticationService.getUsersByType(UserType.STUDENT);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(testUser));
            assertTrue(result.contains(studentUser));
            verify(userRepository).findByType(UserType.STUDENT);
        }

        /**
         * Tests retrieval when no users of specified type exist.
         */
        @Test
        @DisplayName("Should return empty list when no users of specified type exist")
        void getUsersByType_NoUsersOfType_ShouldReturnEmptyList() {
            when(userRepository.findByType(UserType.PROFESSOR)).thenReturn(Arrays.asList());

            List<User> result = authenticationService.getUsersByType(UserType.PROFESSOR);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findByType(UserType.PROFESSOR);
        }
    }

    @Nested
    @DisplayName("toggleUserStatus() Tests")
    class ToggleUserStatusTests {

        /**
         * Tests successful status toggle for active user.
         */
        @Test
        @DisplayName("Should toggle user status from active to inactive")
        void toggleUserStatus_ActiveUser_ShouldDeactivate() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = authenticationService.toggleUserStatus("user123");

            assertNotNull(result);
            assertFalse(testUser.isActive());
            verify(userRepository).findById("user123");
            verify(userRepository).save(testUser);
        }

        /**
         * Tests successful status toggle for inactive user.
         */
        @Test
        @DisplayName("Should toggle user status from inactive to active")
        void toggleUserStatus_InactiveUser_ShouldActivate() {
            testUser.setActive(false);
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = authenticationService.toggleUserStatus("user123");

            assertNotNull(result);
            assertTrue(testUser.isActive());
            verify(userRepository).findById("user123");
            verify(userRepository).save(testUser);
        }

        /**
         * Tests exception when user is not found.
         */
        @Test
        @DisplayName("Should throw exception when user is not found")
        void toggleUserStatus_UserNotFound_ShouldThrowException() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.toggleUserStatus("nonexistent")
            );

            assertEquals("User not found", exception.getMessage());
            verify(userRepository).findById("nonexistent");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("searchUsersByName() Tests")
    class SearchUsersByNameTests {

        /**
         * Tests search by name with matching results.
         */
        @Test
        @DisplayName("Should return users whose names contain search term")
        void searchUsersByName_MatchingUsers_ShouldReturnUsers() {
            List<User> expectedUsers = Arrays.asList(testUser);
            when(userRepository.findByNameContainingIgnoreCase("Juan")).thenReturn(expectedUsers);

            List<User> result = authenticationService.searchUsersByName("Juan");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains(testUser));
            verify(userRepository).findByNameContainingIgnoreCase("Juan");
        }

        /**
         * Tests search by name with no matching results.
         */
        @Test
        @DisplayName("Should return empty list when no users match search term")
        void searchUsersByName_NoMatches_ShouldReturnEmptyList() {
            when(userRepository.findByNameContainingIgnoreCase("Nonexistent")).thenReturn(Arrays.asList());

            List<User> result = authenticationService.searchUsersByName("Nonexistent");

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findByNameContainingIgnoreCase("Nonexistent");
        }
    }

    @Nested
    @DisplayName("userExistsByDocument() Tests")
    class UserExistsByDocumentTests {

        /**
         * Tests document existence check when user exists.
         */
        @Test
        @DisplayName("Should return true when user with document exists")
        void userExistsByDocument_UserExists_ShouldReturnTrue() {
            when(userRepository.existsByDocument("12345678")).thenReturn(true);

            boolean result = authenticationService.userExistsByDocument("12345678");

            assertTrue(result);
            verify(userRepository).existsByDocument("12345678");
        }

        /**
         * Tests document existence check when user does not exist.
         */
        @Test
        @DisplayName("Should return false when user with document does not exist")
        void userExistsByDocument_UserDoesNotExist_ShouldReturnFalse() {
            when(userRepository.existsByDocument("nonexistent")).thenReturn(false);

            boolean result = authenticationService.userExistsByDocument("nonexistent");

            assertFalse(result);
            verify(userRepository).existsByDocument("nonexistent");
        }
    }

    @Nested
    @DisplayName("userExistsByEmail() Tests")
    class UserExistsByEmailTests {

        /**
         * Tests email existence check when user exists.
         */
        @Test
        @DisplayName("Should return true when user with email exists")
        void userExistsByEmail_UserExists_ShouldReturnTrue() {
            when(userRepository.existsByMail("juan.perez@escuelaing.edu.co")).thenReturn(true);

            boolean result = authenticationService.userExistsByEmail("juan.perez@escuelaing.edu.co");

            assertTrue(result);
            verify(userRepository).existsByMail("juan.perez@escuelaing.edu.co");
        }

        /**
         * Tests email existence check when user does not exist.
         */
        @Test
        @DisplayName("Should return false when user with email does not exist")
        void userExistsByEmail_UserDoesNotExist_ShouldReturnFalse() {
            when(userRepository.existsByMail("nonexistent@example.com")).thenReturn(false);

            boolean result = authenticationService.userExistsByEmail("nonexistent@example.com");

            assertFalse(result);
            verify(userRepository).existsByMail("nonexistent@example.com");
        }
    }

    @Nested
    @DisplayName("AuthenticationResult Tests")
    class AuthenticationResultTests {

        /**
         * Tests AuthenticationResult creation and getters.
         */
        @Test
        @DisplayName("Should create AuthenticationResult with correct values")
        void authenticationResult_ShouldCreateWithCorrectValues() {
            AuthenticationResult result = new AuthenticationResult(true, "Success message", testUser);

            assertTrue(result.isSuccess());
            assertEquals("Success message", result.getMessage());
            assertEquals(testUser, result.getUser());
        }

        /**
         * Tests AuthenticationResult with failure case.
         */
        @Test
        @DisplayName("Should create AuthenticationResult for failure case")
        void authenticationResult_FailureCase_ShouldCreateCorrectly() {
            AuthenticationResult result = new AuthenticationResult(false, "Error message", null);

            assertFalse(result.isSuccess());
            assertEquals("Error message", result.getMessage());
            assertNull(result.getUser());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        /**
         * Tests that the service can be instantiated with dependencies.
         */
        @Test
        @DisplayName("Should create service with repository and encoder dependencies")
        void constructor_ShouldCreateServiceWithDependencies() {
            UserRepository repository = mock(UserRepository.class);
            PasswordEncoder encoder = mock(PasswordEncoder.class);

            AuthenticationService service = new AuthenticationService(repository, encoder);

            assertNotNull(service);
        }
    }
}