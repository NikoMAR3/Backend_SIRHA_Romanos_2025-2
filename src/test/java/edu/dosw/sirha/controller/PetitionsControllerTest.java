package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.controller.dtos.PetitionResponseDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.PetitionService;
import edu.dosw.sirha.model.services.DeanService;
import edu.dosw.sirha.model.components.util.PetitionCreator;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetitionsController Tests")
class PetitionsControllerTest {

    @Mock
    private PetitionService petitionService;

    @Mock
    private DeanService deanService;

    @Mock
    private List<PetitionCreator> petitionCreators;

    @Mock
    private PetitionCreator petitionCreator;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PetitionsController petitionsController;

    private Student testStudent;
    private Dean testDean;
    private AcademicVicePresident testVP;
    private Petition testPetition;
    private PetitionRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // Setup test student
        testStudent = new Student();
        testStudent.setId("student123");
        testStudent.setType(UserType.STUDENT);
        testStudent.setName("Test Student");

        // Setup test dean
        testDean = new Dean();
        testDean.setId("dean123");
        testDean.setType(UserType.DEAN);
        testDean.setName("Test Dean");
        Deanery deanery = new Deanery();
        deanery.setDeaneryName("Engineering");
        testDean.setDeanery(deanery);

        // Setup test VP
        testVP = new AcademicVicePresident();
        testVP.setId("vp123");
        testVP.setType(UserType.ACADEMIC_VICEPRESIDENT);
        testVP.setName("Test VP");

        // Setup test petition
        testPetition = new Petition();
        testPetition.setPetitionId("PET001");
        testPetition.setStudentId("student123");
        testPetition.setType(PetitionType.ADD_SUBJECT);
        testPetition.setSubjectId("MATH101");
        testPetition.setSubjectShortName("MAT101");
        testPetition.setSubjectName("Mathematics 101");
        testPetition.setAssociateDeanery("Engineering");
        testPetition.setPriority(PetitionPriority.MEDIUM);
        testPetition.setState(PetitionState.PENDING);
        testPetition.setCreationDate(LocalDateTime.now());
        testPetition.setJustification("Need to add this subject");

        // Setup test request
        testRequest = new PetitionRequestDTO();
        testRequest.setUserID("student123");
        testRequest.setType(PetitionType.ADD_SUBJECT);
    }

    @Nested
    @DisplayName("Create Petition Tests")
    class CreatePetitionTests {

        @Test
        @DisplayName("Should create petition successfully for student")
        void shouldCreatePetitionSuccessfully() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testStudent);

                when(petitionCreators.stream()).thenReturn(Arrays.asList(petitionCreator).stream());
                when(petitionCreator.supports(any())).thenReturn(true);
                when(petitionCreator.createPetition(any())).thenReturn(testPetition);
                when(petitionService.createPetition(any())).thenReturn(testPetition);

                // Act
                ResponseEntity<PetitionResponseDTO> response = petitionsController.createPetition(testRequest, session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("PET001", response.getBody().getPetitionId());
                verify(petitionService).createPetition(any());
            }
        }

        @Test
        @DisplayName("Should throw exception when student tries to create petition for another student")
        void shouldThrowExceptionWhenStudentCreatesForAnother() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                testRequest.setUserID("otherStudent");
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testStudent);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                    petitionsController.createPetition(testRequest, session);
                });
            }
        }

    }

    @Nested
    @DisplayName("Get Petition By ID Tests")
    class GetPetitionByIdTests {

        @Test
        @DisplayName("Should retrieve petition successfully for owner student")
        void shouldRetrievePetitionForOwner() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(session))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testStudent);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(testPetition);

                // Act
                ResponseEntity<PetitionResponseDTO> response = petitionsController.getPetitionById("PET001", session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("PET001", response.getBody().getPetitionId());
            }
        }

        @Test
        @DisplayName("Should throw exception when student tries to access another student's petition")
        void shouldThrowExceptionWhenAccessingOtherStudentPetition() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                testPetition.setStudentId("otherStudent");
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(session))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testStudent);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(testPetition);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                    petitionsController.getPetitionById("PET001", session);
                });
            }
        }

        @Test
        @DisplayName("Should allow dean to access petition from their deanery")
        void shouldAllowDeanToAccessOwnDeaneryPetition() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(session))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(testPetition);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);

                // Act
                ResponseEntity<PetitionResponseDTO> response = petitionsController.getPetitionById("PET001", session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
            }
        }
    }

    @Nested
    @DisplayName("Get All Petitions Tests")
    class GetAllPetitionsTests {

        @Test
        @DisplayName("Should retrieve all petitions for dean")
        void shouldRetrieveAllPetitionsForDean() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<List<PetitionResponseDTO>> response = petitionsController.getAllPetitions(session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("Should retrieve all petitions for VP")
        void shouldRetrieveAllPetitionsForVP() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchAllPetitions()).thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<List<PetitionResponseDTO>> response = petitionsController.getAllPetitions(session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(1, response.getBody().size());
            }
        }
    }

    @Nested
    @DisplayName("Get Pending Petitions Tests")
    class GetPendingPetitionsTests {

        @Test
        @DisplayName("Should retrieve pending petitions for dean")
        void shouldRetrievePendingPetitionsForDean() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByState(PetitionState.PENDING))
                        .thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<List<PetitionResponseDTO>> response = petitionsController.getPendingPetitions(session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(1, response.getBody().size());
            }
        }
    }

    @Nested
    @DisplayName("Get Petitions By Student Tests")
    class GetPetitionsByStudentTests {

        @Test
        @DisplayName("Should retrieve own petitions for student")
        void shouldRetrieveOwnPetitions() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(session))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testStudent);
                when(petitionService.searchPetitionsByStudentId("student123"))
                        .thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByStudent("student123", session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("Should throw exception when student tries to view other student's petitions")
        void shouldThrowExceptionWhenViewingOtherStudentPetitions() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(session))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testStudent);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                    petitionsController.getPetitionsByStudent("otherStudent", session);
                });
            }
        }
    }

    @Nested
    @DisplayName("Change Petition State Tests")
    class ChangePetitionStateTests {

        @Test
        @DisplayName("Should change petition state successfully for dean")
        void shouldChangePetitionStateForDean() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(testPetition);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);

                Petition approvedPetition = new Petition();
                approvedPetition.setPetitionId("PET001");
                approvedPetition.setState(PetitionState.APPROVED);
                approvedPetition.setStudentId("student123");
                approvedPetition.setType(PetitionType.ADD_SUBJECT);
                approvedPetition.setAssociateDeanery("Engineering");

                when(petitionService.changePetitionState("PET001", PetitionState.APPROVED))
                        .thenReturn(approvedPetition);

                // Act
                ResponseEntity<PetitionResponseDTO> response =
                        petitionsController.changePetitionState("PET001", PetitionState.APPROVED, session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(petitionService).changePetitionState("PET001", PetitionState.APPROVED);
            }
        }
    }

    @Nested
    @DisplayName("Change Petition Priority Tests")
    class ChangePetitionPriorityTests {

        @Test
        @DisplayName("Should change petition priority successfully")
        void shouldChangePetitionPriority() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(testPetition);

                Petition updatedPetition = new Petition();
                updatedPetition.setPetitionId("PET001");
                updatedPetition.setPriority(PetitionPriority.HIGH);
                updatedPetition.setStudentId("student123");
                updatedPetition.setType(PetitionType.ADD_SUBJECT);
                updatedPetition.setAssociateDeanery("Engineering");

                when(petitionService.changePetitionPriority("PET001", PetitionPriority.HIGH))
                        .thenReturn(updatedPetition);

                // Act
                ResponseEntity<PetitionResponseDTO> response =
                        petitionsController.changePetitionPriority("PET001", PetitionPriority.HIGH, session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(petitionService).changePetitionPriority("PET001", PetitionPriority.HIGH);
            }
        }
    }

    @Nested
    @DisplayName("Delete Petition Tests")
    class DeletePetitionTests {

        @Test
        @DisplayName("Should delete petition successfully for VP")
        void shouldDeletePetitionForVP() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.deletePetition("PET001")).thenReturn(true);

                // Act
                ResponseEntity<Void> response = petitionsController.deletePetition("PET001", session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                verify(petitionService).deletePetition("PET001");
            }
        }

        @Test
        @DisplayName("Should return 404 when petition not found")
        void shouldReturn404WhenPetitionNotFound() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.deletePetition("PET001")).thenReturn(false);

                // Act
                ResponseEntity<Void> response = petitionsController.deletePetition("PET001", session);

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            }
        }

        @Test
        @DisplayName("Should throw exception when dean tries to delete petition")
        void shouldThrowExceptionWhenDeanTriesToDelete() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                    petitionsController.deletePetition("PET001", session);
                });
            }
        }
    }

    @Nested
    @DisplayName("Statistics and Reports Tests")
    class StatisticsAndReportsTests {

        @Test
        @DisplayName("Should generate petition statistics for dean")
        void shouldGenerateStatistics() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<Map<String, Object>> response =
                        petitionsController.getPetitionStatistics(session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody().containsKey("totalPetitions"));
            }
        }

        @Test
        @DisplayName("Should generate approval rate report")
        void shouldGenerateApprovalRateReport() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchAllPetitions()).thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<Map<String, Object>> response =
                        petitionsController.getApprovalRateReport(session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().containsKey("approvalRate"));
            }
        }
    }

    @Nested
    @DisplayName("Get Petitions By Deanery Tests")
    class GetPetitionsByDeaneryTests {

        @Test
        @DisplayName("Should retrieve petitions for own deanery")
        void shouldRetrievePetitionsForOwnDeanery() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(Arrays.asList(testPetition));

                // Act
                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByDeanery("Engineering", session);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("Should throw exception when dean tries to access another deanery")
        void shouldThrowExceptionWhenAccessingOtherDeanery() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                // Arrange
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                    petitionsController.getPetitionsByDeanery("Science", session);
                });
            }
        }
    }
    @Test
    @DisplayName("Should return empty report if no petitions for deanery")
    void shouldReturnEmptyApprovalRateReportForDeanery() {
        try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
            // Arrange
            authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                    .thenReturn(null);
            authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                    .thenReturn(testVP);
            when(petitionService.searchPetitionsByDeanery(anyString())).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<Map<String, Object>> response =
                    petitionsController.getApprovalRateByDeanery("Science", session);

            // Assert
            assertNotNull(response.getBody());
            assertEquals(0L, response.getBody().get("totalPetitions"));
        }
    }

    @Test
    @DisplayName("Should return empty report if no petitions for subject")
    void shouldReturnEmptyApprovalRateReportForSubject() {
        try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
            // Arrange
            authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any()))
                    .thenReturn(null);
            authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                    .thenReturn(testVP);
            when(petitionService.searchPetitionsBySubjectId(anyString())).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<Map<String, Object>> response =
                    petitionsController.getApprovalRateBySubject("MATH999", session);

            // Assert
            assertNotNull(response.getBody());
            assertEquals(0L, response.getBody().get("totalPetitions"));
        }
    }
    @Test
    @DisplayName("Should return stats with empty list")
    void testGenerateBasicReassignmentStatsEmpty() throws Exception {
        // Llama el método privado por reflexión
        Method method = PetitionsController.class.getDeclaredMethod("generateBasicReassignmentStats", List.class);
        method.setAccessible(true);
        Map<String, Object> result = (Map<String, Object>) method.invoke(petitionsController, Collections.emptyList());
        assertEquals(0, result.get("totalReassignments"));
        assertEquals(0.0, result.get("successRate"));
    }

    @Test
    @DisplayName("Should return stats with various petitions")
    void testGenerateBasicReassignmentStatsWithData() throws Exception {
        Petition p1 = new Petition();
        p1.setSubjectShortName("MAT101");
        p1.setState(PetitionState.APPROVED);
        p1.setType(PetitionType.CHANGE_GROUP);

        Petition p2 = new Petition();
        p2.setSubjectShortName("PHY101");
        p2.setState(PetitionState.PENDING);
        p2.setType(PetitionType.CHANGE_GROUP);

        Petition p3 = new Petition();
        p3.setSubjectShortName("MAT101");
        p3.setState(PetitionState.REPROVED);
        p3.setType(PetitionType.CHANGE_GROUP);

        List<Petition> petitions = Arrays.asList(p1, p2, p3);

        Method method = PetitionsController.class.getDeclaredMethod("generateBasicReassignmentStats", List.class);
        method.setAccessible(true);
        Map<String, Object> result = (Map<String, Object>) method.invoke(petitionsController, petitions);

        assertEquals(3, result.get("totalReassignments"));
        assertEquals(1L, ((Map)result.get("bySubject")).get("PHY101"));
        assertEquals(2L, ((Map)result.get("bySubject")).get("MAT101"));
        assertTrue(result.get("topRequestedSubjects") instanceof List);
        assertTrue((double)result.get("successRate") > 0.0);
    }

    @Test
    @DisplayName("Should ignore null subjectShortName in reassignment stats")
    void testGenerateBasicReassignmentStatsIgnoresNullSubjects() throws Exception {
        Petition p1 = new Petition();
        p1.setSubjectShortName(null);
        p1.setType(PetitionType.CHANGE_GROUP);
        p1.setState(PetitionState.APPROVED);
        List<Petition> petitions = Collections.singletonList(p1);

        Method method = PetitionsController.class.getDeclaredMethod("generateBasicReassignmentStats", List.class);
        method.setAccessible(true);
        Map<String, Object> result = (Map<String, Object>) method.invoke(petitionsController, petitions);

        Map<String, Long> bySubject = (Map<String, Long>) result.get("bySubject");
        assertTrue(bySubject.isEmpty());
    }
    @Nested
    @DisplayName("Cobertura getPetitionsByType")
    class GetPetitionsByTypeTests {

        @Test
        @DisplayName("Denies access if not allowed")
        void shouldThrowIfNotAllowed() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                Exception ex = assertThrows(IllegalArgumentException.class, () ->
                        petitionsController.getPetitionsByType(PetitionType.ADD_SUBJECT, session)
                );
                assertTrue(ex.getMessage().contains("No tienes permisos"));
            }
        }

        @Test
        @DisplayName("Returns empty list if no petitions of that type")
        void shouldReturnEmptyList() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionByType(PetitionType.ADD_SUBJECT))
                        .thenReturn(Collections.emptyList());

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByType(PetitionType.ADD_SUBJECT, session);

                assertNotNull(response.getBody());
                assertTrue(response.getBody().isEmpty());
            }
        }

        @Test
        @DisplayName("Returns petitions for VP")
        void shouldReturnPetitionsForVP() {
            Petition p = new Petition();
            p.setType(PetitionType.ADD_SUBJECT);

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionByType(PetitionType.ADD_SUBJECT))
                        .thenReturn(List.of(p));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByType(PetitionType.ADD_SUBJECT, session);

                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("Returns only own deanery petitions for Dean")
        void shouldReturnPetitionsForDean() {
            Petition p1 = new Petition();
            p1.setType(PetitionType.ADD_SUBJECT);
            p1.setAssociateDeanery("Engineering");
            Petition p2 = new Petition();
            p2.setType(PetitionType.ADD_SUBJECT);
            p2.setAssociateDeanery("Science");

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionByType(PetitionType.ADD_SUBJECT))
                        .thenReturn(List.of(p1, p2));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByType(PetitionType.ADD_SUBJECT, session);

                assertEquals(1, response.getBody().size());
                assertEquals("Engineering", response.getBody().get(0).getAssociateDeanery());
            }
        }
    }
    @Nested
    @DisplayName("Cobertura getPetitionsByPriority")
    class GetPetitionsByPriorityTests {

        @Test
        @DisplayName("Denies access if not allowed")
        void shouldThrowIfNotAllowed() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                Exception ex = assertThrows(IllegalArgumentException.class, () ->
                        petitionsController.getPetitionsByPriority(PetitionPriority.HIGH, session)
                );
                assertTrue(ex.getMessage().contains("No tienes permisos"));
            }
        }

        @Test
        @DisplayName("Returns empty list if no petitions of that priority")
        void shouldReturnEmptyList() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsByPriority(PetitionPriority.HIGH))
                        .thenReturn(Collections.emptyList());

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByPriority(PetitionPriority.HIGH, session);

                assertNotNull(response.getBody());
                assertTrue(response.getBody().isEmpty());
            }
        }

        @Test
        @DisplayName("Returns petitions for VP")
        void shouldReturnPetitionsForVP() {
            Petition p = new Petition();
            p.setPriority(PetitionPriority.HIGH);

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsByPriority(PetitionPriority.HIGH))
                        .thenReturn(List.of(p));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByPriority(PetitionPriority.HIGH, session);

                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("Returns only own deanery petitions for Dean")
        void shouldReturnPetitionsForDean() {
            Petition p1 = new Petition();
            p1.setPriority(PetitionPriority.HIGH);
            p1.setAssociateDeanery("Engineering");
            Petition p2 = new Petition();
            p2.setPriority(PetitionPriority.HIGH);
            p2.setAssociateDeanery("Science");

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByPriority(PetitionPriority.HIGH))
                        .thenReturn(List.of(p1, p2));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getPetitionsByPriority(PetitionPriority.HIGH, session);

                assertEquals(1, response.getBody().size());
                assertEquals("Engineering", response.getBody().get(0).getAssociateDeanery());
            }
        }
    }
    @Nested
    @DisplayName("Cobertura getApprovedPetitions y getRejectedPetitions")
    class GetApprovedAndRejectedPetitionsTests {

        @Test
        @DisplayName("getApprovedPetitions - acceso denegado")
        void getApprovedPetitions_denied() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                Exception ex = assertThrows(IllegalArgumentException.class, () ->
                        petitionsController.getApprovedPetitions(session)
                );
                assertTrue(ex.getMessage().contains("No tienes permisos"));
            }
        }

        @Test
        @DisplayName("getRejectedPetitions - acceso denegado")
        void getRejectedPetitions_denied() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                Exception ex = assertThrows(IllegalArgumentException.class, () ->
                        petitionsController.getRejectedPetitions(session)
                );
                assertTrue(ex.getMessage().contains("No tienes permisos"));
            }
        }

        @Test
        @DisplayName("getApprovedPetitions - lista vacía")
        void getApprovedPetitions_empty() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsByState(PetitionState.APPROVED))
                        .thenReturn(Collections.emptyList());

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getApprovedPetitions(session);

                assertNotNull(response.getBody());
                assertTrue(response.getBody().isEmpty());
            }
        }

        @Test
        @DisplayName("getRejectedPetitions - lista vacía")
        void getRejectedPetitions_empty() {
            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsByState(PetitionState.REPROVED))
                        .thenReturn(Collections.emptyList());

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getRejectedPetitions(session);

                assertNotNull(response.getBody());
                assertTrue(response.getBody().isEmpty());
            }
        }

        @Test
        @DisplayName("getApprovedPetitions - lista con datos (VP)")
        void getApprovedPetitions_withDataVP() {
            Petition p = new Petition();
            p.setState(PetitionState.APPROVED);

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsByState(PetitionState.APPROVED))
                        .thenReturn(List.of(p));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getApprovedPetitions(session);

                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("getRejectedPetitions - lista con datos (VP)")
        void getRejectedPetitions_withDataVP() {
            Petition p = new Petition();
            p.setState(PetitionState.REPROVED);

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testVP);
                when(petitionService.searchPetitionsByState(PetitionState.REPROVED))
                        .thenReturn(List.of(p));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getRejectedPetitions(session);

                assertEquals(1, response.getBody().size());
            }
        }

        @Test
        @DisplayName("getApprovedPetitions - solo decanatura del decano")
        void getApprovedPetitions_withDataDean() {
            Petition p1 = new Petition();
            p1.setState(PetitionState.APPROVED);
            p1.setAssociateDeanery("Engineering");
            Petition p2 = new Petition();
            p2.setState(PetitionState.APPROVED);
            p2.setAssociateDeanery("Science");

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByState(PetitionState.APPROVED))
                        .thenReturn(List.of(p1, p2));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getApprovedPetitions(session);

                assertEquals(1, response.getBody().size());
                assertEquals("Engineering", response.getBody().get(0).getAssociateDeanery());
            }
        }

        @Test
        @DisplayName("getRejectedPetitions - solo decanatura del decano")
        void getRejectedPetitions_withDataDean() {
            Petition p1 = new Petition();
            p1.setState(PetitionState.REPROVED);
            p1.setAssociateDeanery("Engineering");
            Petition p2 = new Petition();
            p2.setState(PetitionState.REPROVED);
            p2.setAssociateDeanery("Science");

            try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
                authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                        .thenReturn(null);
                authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(testDean);
                when(deanService.searchDeanByCode("dean123")).thenReturn(testDean);
                when(petitionService.searchPetitionsByState(PetitionState.REPROVED))
                        .thenReturn(List.of(p1, p2));

                ResponseEntity<List<PetitionResponseDTO>> response =
                        petitionsController.getRejectedPetitions(session);

                assertEquals(1, response.getBody().size());
                assertEquals("Engineering", response.getBody().get(0).getAssociateDeanery());
            }
        }
    }
    @Test
    @DisplayName("getReassignmentStatistics - acceso denegado")
    void getReassignmentStatistics_denied() {
        try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
            authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                    .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

            Exception ex = assertThrows(IllegalArgumentException.class, () ->
                    petitionsController.getReassignmentStatistics(session)
            );
            assertTrue(ex.getMessage().contains("No tienes permisos"));
        }
    }

    @Test
    @DisplayName("getReassignmentStatistics - lista vacía")
    void getReassignmentStatistics_empty() {
        try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
            authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                    .thenReturn(null);
            authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                    .thenReturn(testVP);

            // Simula que no hay petitions de tipo CHANGE_GROUP
            when(petitionService.searchAllPetitions()).thenReturn(Collections.emptyList());

            ResponseEntity<Map<String,Object>> response =
                    petitionsController.getReassignmentStatistics(session);

            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().get("totalReassignments"));
        }
    }

    @Test
    @DisplayName("getReassignmentStatistics - lista con datos (VP)")
    void getReassignmentStatistics_withDataVP() {
        Petition p = new Petition();
        p.setType(PetitionType.CHANGE_GROUP);
        p.setSubjectShortName("MAT101");
        p.setState(PetitionState.APPROVED);

        try (MockedStatic<AuthValidationUtils> authUtils = mockStatic(AuthValidationUtils.class)) {
            authUtils.when(() -> AuthValidationUtils.validateAuthentication(eq(session), any(), any()))
                    .thenReturn(null);
            authUtils.when(() -> AuthValidationUtils.getCurrentUser(session))
                    .thenReturn(testVP);

            when(petitionService.searchAllPetitions()).thenReturn(List.of(p));

            ResponseEntity<Map<String,Object>> response =
                    petitionsController.getReassignmentStatistics(session);

            assertEquals(1, response.getBody().get("totalReassignments"));
            assertTrue(((Map<String,Long>)response.getBody().get("bySubject")).containsKey("MAT101"));
        }
    }


    private void invokeValidatePetitionAccess(User user, Petition petition) throws Exception {
        Method m = PetitionsController.class.getDeclaredMethod("validatePetitionAccess", User.class, Petition.class);
        m.setAccessible(true);
        m.invoke(petitionsController, user, petition);
    }
}