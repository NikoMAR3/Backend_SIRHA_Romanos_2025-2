package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.AuthDto;
import edu.dosw.sirha.model.entities.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthValidationUtils Tests")
class AuthValidationUtilsTest {

    @Mock
    private HttpSession session;

    private Student testStudent;
    private Dean testDean;
    private Professor testProfessor;
    private AcademicVicePresident testVP;

    @BeforeEach
    void setUp() {

        testStudent = new Student();
        testStudent.setId("student123");
        testStudent.setType(UserType.STUDENT);
        testStudent.setName("Test Student");


        testDean = new Dean();
        testDean.setId("dean123");
        testDean.setType(UserType.DEAN);
        testDean.setName("Test Dean");


        testProfessor = new Professor();
        testProfessor.setId("prof123");
        testProfessor.setType(UserType.PROFESSOR);
        testProfessor.setName("Test Professor");


        testVP = new AcademicVicePresident();
        testVP.setId("vp123");
        testVP.setType(UserType.ACADEMIC_VICEPRESIDENT);
        testVP.setName("Test VP");
    }

    @Nested
    @DisplayName("Validate Authentication Tests")
    class ValidateAuthenticationTest {

        @Test
        @DisplayName("Should return null when user is authenticated and no roles specified")
        void shouldReturnNullWhenAuthenticatedNoRoles() {

            when(session.getAttribute("user")).thenReturn(testStudent);


            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(session);


            assertNull(result);
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() {

            when(session.getAttribute("user")).thenReturn(null);


            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(session);

            assertNotNull(result);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertTrue(result.getBody() instanceof AuthDto.ApiResponse);
            AuthDto.ApiResponse response = (AuthDto.ApiResponse) result.getBody();
            assertFalse(response.isSuccess());
            assertEquals("User not authenticated", response.getMessage());
        }

        @Test
        @DisplayName("Should return null when user has required role")
        void shouldReturnNullWhenUserHasRequiredRole() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(
                    session, UserType.STUDENT
            );

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should return 403 when user does not have required role")
        void shouldReturn403WhenUserDoesNotHaveRole() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(
                    session, UserType.DEAN
            );

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertTrue(result.getBody() instanceof AuthDto.ApiResponse);
            AuthDto.ApiResponse response = (AuthDto.ApiResponse) result.getBody();
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Access denied for role"));
        }

        @Test
        @DisplayName("Should allow access when user has one of multiple allowed roles")
        void shouldAllowAccessWithMultipleRoles() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testDean);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(
                    session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT
            );

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should deny access when user does not have any of multiple allowed roles")
        void shouldDenyAccessWithMultipleRoles() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(
                    session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT
            );

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        }

        @Test
        @DisplayName("Should handle empty allowed roles array")
        void shouldHandleEmptyAllowedRoles() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(session);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should validate all user types correctly")
        void shouldValidateAllUserTypes() {
            // Test STUDENT
            when(session.getAttribute("user")).thenReturn(testStudent);
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.STUDENT));

            // Test DEAN
            when(session.getAttribute("user")).thenReturn(testDean);
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.DEAN));

            // Test PROFESSOR
            when(session.getAttribute("user")).thenReturn(testProfessor);
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.PROFESSOR));

            // Test ACADEMIC_VICEPRESIDENT
            when(session.getAttribute("user")).thenReturn(testVP);
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT));
        }

        @Test
        @DisplayName("Should verify session attribute is checked")
        void shouldVerifySessionAttributeChecked() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            AuthValidationUtils.validateAuthentication(session);

            // Assert
            verify(session).getAttribute("user");
        }
    }

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTest {

        @Test
        @DisplayName("Should return user when authenticated")
        void shouldReturnUserWhenAuthenticated() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            User result = AuthValidationUtils.getCurrentUser(session);

            // Assert
            assertNotNull(result);
            assertEquals(testStudent, result);
            assertEquals("student123", result.getId());
            assertEquals(UserType.STUDENT, result.getType());
        }

        @Test
        @DisplayName("Should return null when not authenticated")
        void shouldReturnNullWhenNotAuthenticated() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(null);

            // Act
            User result = AuthValidationUtils.getCurrentUser(session);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should return correct user for different user types")
        void shouldReturnCorrectUserForDifferentTypes() {
            // Test Student
            when(session.getAttribute("user")).thenReturn(testStudent);
            User student = AuthValidationUtils.getCurrentUser(session);
            assertEquals(UserType.STUDENT, student.getType());

            // Test Dean
            when(session.getAttribute("user")).thenReturn(testDean);
            User dean = AuthValidationUtils.getCurrentUser(session);
            assertEquals(UserType.DEAN, dean.getType());

            // Test Professor
            when(session.getAttribute("user")).thenReturn(testProfessor);
            User professor = AuthValidationUtils.getCurrentUser(session);
            assertEquals(UserType.PROFESSOR, professor.getType());

            // Test VP
            when(session.getAttribute("user")).thenReturn(testVP);
            User vp = AuthValidationUtils.getCurrentUser(session);
            assertEquals(UserType.ACADEMIC_VICEPRESIDENT, vp.getType());
        }

        @Test
        @DisplayName("Should call session getAttribute")
        void shouldCallSessionGetAttribute() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            AuthValidationUtils.getCurrentUser(session);

            // Assert
            verify(session).getAttribute("user");
        }

        @Test
        @DisplayName("Should handle casting correctly")
        void shouldHandleCastingCorrectly() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testDean);

            // Act
            User result = AuthValidationUtils.getCurrentUser(session);

            // Assert
            assertNotNull(result);
            assertInstanceOf(Dean.class, result);
            Dean dean = (Dean) result;
            assertEquals("dean123", dean.getId());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTest {

        @Test
        @DisplayName("Should validate and get user in sequence")
        void shouldValidateAndGetUserInSequence() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            ResponseEntity<?> validation = AuthValidationUtils.validateAuthentication(session);
            User user = AuthValidationUtils.getCurrentUser(session);

            // Assert
            assertNull(validation);
            assertNotNull(user);
            assertEquals(testStudent, user);
        }

        @Test
        @DisplayName("Should handle role checking and user retrieval")
        void shouldHandleRoleCheckingAndUserRetrieval() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testDean);

            // Act
            ResponseEntity<?> validation = AuthValidationUtils.validateAuthentication(
                    session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT
            );
            User user = AuthValidationUtils.getCurrentUser(session);

            // Assert
            assertNull(validation);
            assertNotNull(user);
            assertEquals(UserType.DEAN, user.getType());
        }

        @Test
        @DisplayName("Should properly reject unauthorized access")
        void shouldProperlyRejectUnauthorizedAccess() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testStudent);

            // Act
            ResponseEntity<?> validation = AuthValidationUtils.validateAuthentication(
                    session, UserType.DEAN
            );
            User user = AuthValidationUtils.getCurrentUser(session);

            // Assert
            assertNotNull(validation);
            assertEquals(HttpStatus.FORBIDDEN, validation.getStatusCode());
            assertNotNull(user);
            assertEquals(UserType.STUDENT, user.getType());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTest {

        @Test
        @DisplayName("Should handle session with non-User object")
        void shouldHandleNonUserObject() {
            // Arrange
            when(session.getAttribute("user")).thenReturn("not a user");

            // Act & Assert
            assertThrows(ClassCastException.class, () -> {
                AuthValidationUtils.getCurrentUser(session);
            });
        }

        @Test
        @DisplayName("Should handle multiple role checks with same user")
        void shouldHandleMultipleRoleChecks() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testVP);

            // Act & Assert - Multiple calls should be consistent
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT));
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT));
            assertNull(AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT));
        }

        @Test
        @DisplayName("Should handle varargs with single role")
        void shouldHandleVarargsWithSingleRole() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testProfessor);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(
                    session, UserType.PROFESSOR
            );

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle varargs with many roles")
        void shouldHandleVarargsWithManyRoles() {
            // Arrange
            when(session.getAttribute("user")).thenReturn(testDean);

            // Act
            ResponseEntity<?> result = AuthValidationUtils.validateAuthentication(
                    session,
                    UserType.STUDENT,
                    UserType.PROFESSOR,
                    UserType.DEAN,
                    UserType.ACADEMIC_VICEPRESIDENT
            );

            // Assert
            assertNull(result);
        }
    }
}