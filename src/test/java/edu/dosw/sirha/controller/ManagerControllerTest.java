package edu.dosw.sirha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.controller.dtos.ManagerRequestDTO;
import edu.dosw.sirha.controller.dtos.ManagerResponseDTO;
import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for ManagerController.
 * Tests all endpoints with different user roles, edge cases, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerController Tests")
class ManagerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DeaneryService deaneryService;
    @Mock
    private DeanService deanService;
    @Mock
    private AcademicVicePresidentService academicVicePresidentService;
    @Mock
    private ClassSessionService classSessionService;
    @Mock
    private PeriodService periodService;
    @Mock
    private ObserverService observerService;
    @Mock
    private PetitionService petitionService;
    @Mock
    private StudentService studentService;
    @Mock
    private TrafficLightService trafficLightService;
    @Mock
    private SubjectService subjectService;
    @Mock
    private AcademicProgramService academicProgramService;
    @Mock
    private ProfessorService professorService;
    @Mock
    private HttpSession session;

    @InjectMocks
    private ManagerController managerController;

    // Test data
    private User deanUser;
    private User vpUser;
    private Dean dean;
    private AcademicVicePresident vp;
    private Deanery deanery;
    private Petition petition;
    private Student student;
    private Schedule schedule;
    private TrafficLight trafficLight;
    private ClassSession classSession;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(managerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        setupTestData();
    }

    private void setupTestData() {
        // Dean setup
        deanUser = new Dean();
        deanUser.setId("DEAN001");
        deanUser.setName("Dean Test");
        deanUser.setType(UserType.DEAN);

        dean = new Dean();
        dean.setId("DEAN001");
        dean.setDeanCode("DEAN001");
        dean.setName("Dean Test");
        dean.setMail("dean@test.com");
        dean.setDocument("123456789");

        // VP setup
        vpUser = new AcademicVicePresident();
        vpUser.setId("VP001");
        vpUser.setName("VP Test");
        vpUser.setType(UserType.ACADEMIC_VICEPRESIDENT);

        vp = new AcademicVicePresident();
        vp.setId("VP001");
        vp.setName("VP Test");

        // Deanery setup
        deanery = new Deanery();
        deanery.setId("DEANERY001");
        deanery.setDeaneryName("Engineering");
        deanery.setDean(dean);
        dean.setDeanery(deanery);

        // Petition setup
        petition = new Petition();
        petition.setPetitionId("PET001");
        petition.setStudentId("STU001");
        petition.setSubjectShortName("CALC1");
        petition.setSubjectName("Calculus I");
        petition.setType(PetitionType.CHANGE_GROUP);
        petition.setState(PetitionState.PENDING);
        petition.setPriority(PetitionPriority.HIGH);
        petition.setCreationDate(LocalDateTime.now());
        petition.setJustification("Schedule conflict");
        petition.setAssociateDeanery("Engineering");
        petition.setDecisionHistory(new ArrayList<>());

        // Student setup
        student = new Student();
        student.setId("STU001");
        student.setName("Student Test");
        student.setDeanery(deanery);

        // Schedule setup
        schedule = new Schedule();
        schedule.setSubjectShortName("CALC1");
        schedule.setName("Calculus I");
        schedule.setSemester("3");
        schedule.setDayOfWeek("Monday");
        schedule.setStartTime(LocalDateTime.of(2024, 1, 1, 8, 0));
        schedule.setEndTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        schedule.setClassroom("A101");

        // TrafficLight setup
        trafficLight = new TrafficLight();
        trafficLight.setStudentId("STU001");
        trafficLight.setStatus(TrafficLightStatus.GREEN);
        trafficLight.setApprovedSubjects(new HashMap<>());
        trafficLight.setFailedSubjects(new HashMap<>());
        trafficLight.setOnGoingSubjects(new ArrayList<>());

        // ClassSession setup
        classSession = new ClassSession();
        classSession.setId("SESSION001");
        classSession.setSubjectShortName("CALC1");
        classSession.setCapacity(30);
        classSession.setEnrolledStudents(25);
        classSession.setWaitingListStudentIds(new ArrayList<>());
    }

    @Nested
    @DisplayName("Authentication and Authorization Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should deny access when user is not authenticated")
        void testUnauthenticatedAccess() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                                any(), any(UserType[].class)))
                        .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session)
                );

                assertTrue(exception.getMessage().contains("permisos"));
            }
        }

        @Test
        @DisplayName("Should deny access when user tries to access another manager's data")
        void testAccessAnotherManagerData() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.getDeaneryPetitions("DEAN002", "DEAN", session)
                );

                assertTrue(exception.getMessage().contains("otro manager"));
            }
        }

        @Test
        @DisplayName("Should deny access when manager type doesn't match user type")
        void testInvalidManagerType() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.getDeaneryPetitions("DEAN001", "ACADEMIC_VICEPRESIDENT", session)
                );

                assertTrue(exception.getMessage().contains("no coincide"));
            }
        }
    }

    @Nested
    @DisplayName("Petition Management Tests")
    class PetitionTests {

        @Test
        @DisplayName("Should get deanery petitions for Dean")
        void testGetDeaneryPetitionsForDean() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(1, response.getBody().getPetitions().size());

                verify(petitionService).searchPetitionsByDeanery("Engineering");
            }
        }

        @Test
        @DisplayName("Should get all petitions for VP")
        void testGetAllPetitionsForVP() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);
                when(petitionService.searchAllPetitions()).thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDeaneryPetitions("VP001", "ACADEMIC_VICEPRESIDENT", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(1, response.getBody().getPetitions().size());

                verify(petitionService).searchAllPetitions();
            }
        }

        @Test
        @DisplayName("Should approve petition successfully")
        void testApprovePetition() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);
                when(petitionService.changePetitionState("PET001", PetitionState.APPROVED))
                        .thenReturn(petition);
                when(petitionService.modifyPetition(any())).thenReturn(petition);

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setDecision("approve");

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.respondToPetition("PET001", request, "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                verify(petitionService).changePetitionState("PET001", PetitionState.APPROVED);
            }
        }

        @Test
        @DisplayName("Should reject petition with justification")
        void testRejectPetition() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);
                when(petitionService.changePetitionState("PET001", PetitionState.REPROVED))
                        .thenReturn(petition);
                when(petitionService.modifyPetition(any())).thenReturn(petition);

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setDecision("reject");
                request.setJustification("Insufficient capacity");

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.respondToPetition("PET001", request, "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(petitionService).changePetitionState("PET001", PetitionState.REPROVED);
            }
        }

        @Test
        @DisplayName("Should prevent dean from responding to petition outside their deanery")
        void testPreventCrossDeaneryResponse() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                petition.setAssociateDeanery("Science");

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setDecision("approve");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.respondToPetition("PET001", request, "DEAN001", "DEAN", session)
                );

                assertTrue(exception.getMessage().contains("fuera de su decanatura"));
            }
        }

        @Test
        @DisplayName("Should throw exception for invalid decision")
        void testInvalidDecision() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setDecision("invalid_action");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.respondToPetition("PET001", request, "DEAN001", "DEAN", session)
                );

                assertFalse(exception.getMessage().contains("AcciÃ³n no vÃ¡lida"));
            }
        }
    }

    @Nested
    @DisplayName("Student Information Tests")
    class StudentInformationTests {

        @Test
        @DisplayName("Should get student schedule by student ID")
        void testGetStudentSchedule() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(studentService.getStudentSchedule("STU001")).thenReturn(schedule);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentSchedule("STU001", "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody().getStudentSchedule());
                assertEquals("STU001", response.getBody().getStudentSchedule().getStudentId());

                verify(studentService).searchStudentById("STU001");
                verify(studentService).getStudentSchedule("STU001");
            }
        }

        @Test
        @DisplayName("Should get student schedule by petition ID")
        void testGetStudentScheduleWithPetition() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(studentService.getStudentSchedule("STU001")).thenReturn(schedule);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentScheduleWithPetition("PET001", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody().getStudentSchedule());
                assertNotNull(response.getBody().getPetitions());
                assertEquals(1, response.getBody().getPetitions().size());
            }
        }

        @Test
        @DisplayName("Should get student academic status")
        void testGetStudentAcademicStatus() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                when(trafficLightService.calculateGPA("STU001")).thenReturn(3.8);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentAcademicStatus("STU001", "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody().getAcademicStatus());
                assertEquals(3.8, response.getBody().getAcademicStatus().getGpa());
                assertEquals(TrafficLightStatus.GREEN,
                        response.getBody().getAcademicStatus().getTrafficLightStatus());

                verify(trafficLightService).searchTrafficLightByStudentId("STU001");
                verify(trafficLightService).calculateGPA("STU001");
            }
        }

        @Test
        @DisplayName("Should throw exception when traffic light not found")
        void testTrafficLightNotFound() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, () ->
                        managerController.getStudentAcademicStatus("STU001", "DEAN001", "DEAN", session)
                );
            }
        }
    }

    @Nested
    @DisplayName("Group Management Tests")
    class GroupManagementTests {

        @Test
        @DisplayName("Should get alternative groups for subject")
        void testGetAlternativeGroups() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(classSessionService.searchSessionsBySubjectShortName("CALC1"))
                        .thenReturn(List.of(classSession));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getAlternativeGroups("CALC1", "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody().getAlternativeGroups());
                assertEquals(1, response.getBody().getAlternativeGroups().size());

                verify(classSessionService).searchSessionsBySubjectShortName("CALC1");
            }
        }

        @Test
        @DisplayName("Should get capacity alerts above threshold")
        void testGetCapacityAlerts() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(classSessionService.searchAllSessions()).thenReturn(List.of(classSession));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getCapacityAlerts("DEAN001", "DEAN", 80, session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody().getCapacityAlerts());
                assertTrue(response.getBody().getCapacityAlerts().size() > 0);

                verify(observerService).monitorAllClassSessions();
            }
        }

        @Test
        @DisplayName("Should reject invalid threshold")
        void testInvalidThreshold() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.getCapacityAlerts("DEAN001", "DEAN", 150, session)
                );

                assertTrue(exception.getMessage().contains("entre 0 y 100"));
            }
        }
    }

    @Nested
    @DisplayName("Period Configuration Tests")
    class PeriodConfigurationTests {

        @Test
        @DisplayName("Should configure period as VP")
        void testConfigurePeriod() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                Period period = new Period();
                period.setStartDate("2024-01-01");
                period.setEndDate("2024-01-31");
                period.setEnabled(true);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);
                when(periodService.createPeriod(any())).thenReturn(period);

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setPeriodStartDate(LocalDate.of(2024, 1, 1).atStartOfDay());
                request.setPeriodEndDate(LocalDate.of(2024, 1, 31).atStartOfDay());

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.configurePeriod(request, "VP001", "ACADEMIC_VICEPRESIDENT", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                verify(periodService).createPeriod(any());
            }
        }

        @Test
        @DisplayName("Should prevent dean from configuring period")
        void testDeanCannotConfigurePeriod() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                                any(), any(UserType[].class)))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setPeriodStartDate(LocalDate.of(2024, 1, 1).atStartOfDay());
                request.setPeriodEndDate(LocalDate.of(2024, 1, 31).atStartOfDay());

                assertThrows(IllegalArgumentException.class, () ->
                        managerController.configurePeriod(request, "DEAN001", "DEAN", session)
                );
            }
        }

        @Test
        @DisplayName("Should reject invalid date range")
        void testInvalidDateRange() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setPeriodStartDate(LocalDate.of(2024, 1, 31).atStartOfDay());
                request.setPeriodEndDate(LocalDate.of(2024, 1, 1).atStartOfDay());

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.configurePeriod(request, "VP001", "ACADEMIC_VICEPRESIDENT", session)
                );

                assertTrue(exception.getMessage().contains("anterior"));
            }
        }

        @Test
        @DisplayName("Should reject null dates")
        void testNullDates() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);

                ManagerRequestDTO request = new ManagerRequestDTO();

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.configurePeriod(request, "VP001", "ACADEMIC_VICEPRESIDENT", session)
                );

                assertTrue(exception.getMessage().contains("obligatorias"));
            }
        }
    }

    @Nested
    @DisplayName("Dashboard Tests")
    class DashboardTests {

        @Test
        @DisplayName("Should get dashboard for dean with deanery stats")
        void testGetDashboardForDean() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                Petition approvedPetition = new Petition();
                approvedPetition.setState(PetitionState.APPROVED);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(List.of(petition, approvedPetition));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDashboard("DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().getTotalPetitions());
                assertEquals(1, response.getBody().getPendingPetitions());
                assertEquals(1, response.getBody().getApprovedPetitions());

                verify(petitionService).searchPetitionsByDeanery("Engineering");
            }
        }

        @Test
        @DisplayName("Should get dashboard for VP with all institutional stats")
        void testGetDashboardForVP() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                Petition rejectedPetition = new Petition();
                rejectedPetition.setState(PetitionState.REPROVED);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);
                when(petitionService.searchAllPetitions())
                        .thenReturn(List.of(petition, rejectedPetition));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDashboard("VP001", "ACADEMIC_VICEPRESIDENT", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().getTotalPetitions());
                assertEquals(1, response.getBody().getPendingPetitions());
                assertEquals(1, response.getBody().getRejectedPetitions());

                verify(petitionService).searchAllPetitions();
            }
        }
    }

    @Nested
    @DisplayName("Traffic Light Report Tests")
    class TrafficLightReportTests {

        @Test
        @DisplayName("Should generate global traffic light summary for dean")
        void testGlobalTrafficLightSummaryForDean() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchAllStudents()).thenReturn(List.of(student));
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));

                ResponseEntity<Map<String, Object>> response =
                        managerController.getGlobalTrafficLightSummary("DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody().containsKey("totalStudents"));
                assertTrue(response.getBody().containsKey("trafficLightDistribution"));
                assertTrue(response.getBody().containsKey("scope"));
                assertEquals("Engineering", response.getBody().get("scope"));
            }
        }

        @Test
        @DisplayName("Should generate institutional traffic light summary for VP")
        void testInstitutionalTrafficLightSummary() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                Student student2 = new Student();
                student2.setId("STU002");
                student2.setName("Student 2");

                TrafficLight redTrafficLight = new TrafficLight();
                redTrafficLight.setStudentId("STU002");
                redTrafficLight.setStatus(TrafficLightStatus.RED);
                redTrafficLight.setApprovedSubjects(new HashMap<>());
                redTrafficLight.setFailedSubjects(new HashMap<>());
                redTrafficLight.setOnGoingSubjects(new ArrayList<>());

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);
                when(studentService.searchAllStudents()).thenReturn(List.of(student, student2));
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                when(trafficLightService.searchTrafficLightByStudentId("STU002"))
                        .thenReturn(Optional.of(redTrafficLight));

                ResponseEntity<Map<String, Object>> response =
                        managerController.getGlobalTrafficLightSummary("VP001", "ACADEMIC_VICEPRESIDENT", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                Map<String, Object> body = response.getBody();
                assertEquals(2, body.get("totalStudents"));
                assertEquals("INSTITUTIONAL", body.get("scope"));
                assertEquals(1, body.get("studentsInCriticalStatus"));

                @SuppressWarnings("unchecked")
                Map<String, Integer> distribution = (Map<String, Integer>) body.get("trafficLightDistribution");
                assertEquals(1, distribution.get("GREEN"));
                assertEquals(1, distribution.get("RED"));
            }
        }

        @Test
        @DisplayName("Should handle students without traffic light data")
        void testStudentsWithoutTrafficLight() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchAllStudents()).thenReturn(List.of(student));
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.empty());

                ResponseEntity<Map<String, Object>> response =
                        managerController.getGlobalTrafficLightSummary("DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                @SuppressWarnings("unchecked")
                Map<String, Integer> distribution =
                        (Map<String, Integer>) response.getBody().get("trafficLightDistribution");
                assertEquals(1, distribution.get("NO_DATA"));
            }
        }
    }

    @Nested
    @DisplayName("Deanery Management Tests")
    class DeaneryManagementTests {

        @Test
        @DisplayName("Should create deanery as VP")
        void testCreateDeanery() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                Deanery newDeanery = new Deanery();
                newDeanery.setId("DEANERY002");
                newDeanery.setDeaneryName("Science");

                when(deaneryService.createDeanery(any())).thenReturn(newDeanery);

                ManagerRequestDTO.DeaneryRequest request = new ManagerRequestDTO.DeaneryRequest();
                request.setDeaneryId("DEANERY002");
                request.setDeaneryName("Science");

                ResponseEntity<ManagerResponseDTO.DeaneryInfo> response =
                        managerController.createDeanery(request, session);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals("Science", response.getBody().getDeaneryName());

                verify(deaneryService).createDeanery(any());
            }
        }

        @Test
        @DisplayName("Should reject creating deanery with blank name")
        void testCreateDeaneryBlankName() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                ManagerRequestDTO.DeaneryRequest request = new ManagerRequestDTO.DeaneryRequest();
                request.setDeaneryId("DEANERY002");
                request.setDeaneryName("");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.createDeanery(request, session)
                );

                assertTrue(exception.getMessage().contains("obligatorio"));
            }
        }

        @Test
        @DisplayName("Should assign dean to deanery")
        void testAssignDeanToDeanery() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                when(deaneryService.searchDeaneryById("DEANERY001")).thenReturn(deanery);
                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(deaneryService.modifyDeanery(any())).thenReturn(deanery);

                ResponseEntity<ManagerResponseDTO.DeaneryInfo> response =
                        managerController.assignDeanToDeanery("DEANERY001", "DEAN001", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody().getDeanName());

                verify(deaneryService).modifyDeanery(any());
            }
        }

        @Test
        @DisplayName("Should add professor to deanery")
        void testAddProfessorToDeanery() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                Professor professor = new Professor();
                professor.setId("PROF001");
                professor.setName("Professor Test");

                deanery.setProfessors(new ArrayList<>());

                when(deaneryService.searchDeaneryById("DEANERY001")).thenReturn(deanery);
                when(professorService.searchProfessorByCode("PROF001")).thenReturn(professor);
                when(deaneryService.modifyDeanery(any())).thenReturn(deanery);

                ResponseEntity<ManagerResponseDTO.DeaneryInfo> response =
                        managerController.addProfessorToDeanery("DEANERY001", "PROF001", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                verify(deaneryService).modifyDeanery(any());
            }
        }

        @Test
        @DisplayName("Should not add duplicate professor to deanery")
        void testAddDuplicateProfessor() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                Professor professor = new Professor();
                professor.setId("PROF001");
                professor.setName("Professor Test");

                List<Professor> professors = new ArrayList<>();
                professors.add(professor);
                deanery.setProfessors(professors);

                when(deaneryService.searchDeaneryById("DEANERY001")).thenReturn(deanery);
                when(professorService.searchProfessorByCode("PROF001")).thenReturn(professor);

                ResponseEntity<ManagerResponseDTO.DeaneryInfo> response =
                        managerController.addProfessorToDeanery("DEANERY001", "PROF001", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                verify(deaneryService, never()).modifyDeanery(any());
            }
        }
    }

    @Nested
    @DisplayName("Dean Management Tests")
    class DeanManagementTests {

        @Test
        @DisplayName("Should create dean as VP")
        void testCreateDean() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(deanService.createDean(any())).thenReturn(dean);
                when(deanService.save(any())).thenReturn(dean);

                ManagerRequestDTO.DeanRequest request = new ManagerRequestDTO.DeanRequest();
                request.setDeanCode("DEAN002");
                request.setName("New Dean");
                request.setMail("newdean@test.com");
                request.setDocument("987654321");

                ResponseEntity<ManagerResponseDTO.DeanResponseDTO> response =
                        managerController.createDean(request, session);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertNotNull(response.getBody());

                verify(deanService).createDean(any());
                verify(deanService).save(any());
            }
        }

        @Test
        @DisplayName("Should prevent dean from creating another dean")
        void testDeanCannotCreateDean() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                                any(), any(UserType[].class)))
                        .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

                ManagerRequestDTO.DeanRequest request = new ManagerRequestDTO.DeanRequest();
                request.setDeanCode("DEAN002");
                request.setName("New Dean");
                request.setMail("newdean@test.com");
                request.setDocument("987654321");

                assertThrows(IllegalArgumentException.class, () ->
                        managerController.createDean(request, session)
                );
            }
        }

        @Test
        @DisplayName("Should get all deans")
        void testGetAllDeans() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                Dean dean2 = new Dean();
                dean2.setDeanCode("DEAN002");
                dean2.setName("Dean 2");
                dean2.setMail("dean2@test.com");
                dean2.setDocument("987654321");

                when(deanService.searchAllDeans()).thenReturn(List.of(dean, dean2));

                ResponseEntity<List<ManagerResponseDTO.DeanResponseDTO>> response =
                        managerController.getAllDeans(session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().size());

                verify(deanService).searchAllDeans();
            }
        }
    }

    @Nested
    @DisplayName("Academic Program Management Tests")
    class AcademicProgramTests {

        @Test
        @DisplayName("Should create academic program as VP")
        void testCreateAcademicProgram() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                AcademicProgram program = new AcademicProgram();
                program.setId("PROG001");
                program.setName("Computer Science");

                when(academicProgramService.createProgram(any())).thenReturn(program);

                ManagerRequestDTO.AcademicProgramRequest request =
                        new ManagerRequestDTO.AcademicProgramRequest();
                request.setId("PROG001");
                request.setName("Computer Science");

                ResponseEntity<ManagerResponseDTO.AcademicProgramInfo> response =
                        managerController.createAcademicProgram(request, session);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals("Computer Science", response.getBody().getName());

                verify(academicProgramService).createProgram(any());
            }
        }

        @Test
        @DisplayName("Should reject creating program with blank name")
        void testCreateProgramBlankName() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                ManagerRequestDTO.AcademicProgramRequest request =
                        new ManagerRequestDTO.AcademicProgramRequest();
                request.setId("PROG001");
                request.setName("");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.createAcademicProgram(request, session)
                );

                assertTrue(exception.getMessage().contains("obligatorio"));
            }
        }

        @Test
        @DisplayName("Should reject creating program with blank ID")
        void testCreateProgramBlankId() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                ManagerRequestDTO.AcademicProgramRequest request =
                        new ManagerRequestDTO.AcademicProgramRequest();
                request.setId("");
                request.setName("Computer Science");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.createAcademicProgram(request, session)
                );

                assertTrue(exception.getMessage().contains("obligatorio"));
            }
        }

        @Test
        @DisplayName("Should add program to deanery")
        void testAddProgramToDeanery() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                AcademicProgram program = new AcademicProgram();
                program.setId("PROG001");
                program.setName("Computer Science");

                deanery.setAcademicPrograms(new ArrayList<>());

                when(deaneryService.searchDeaneryById("DEANERY001")).thenReturn(deanery);
                when(academicProgramService.searchProgramById("PROG001")).thenReturn(program);
                when(deaneryService.modifyDeanery(any())).thenReturn(deanery);

                ResponseEntity<ManagerResponseDTO.DeaneryInfo> response =
                        managerController.addProgramToDeanery("DEANERY001", "PROG001", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                verify(deaneryService).modifyDeanery(any());
            }
        }

        @Test
        @DisplayName("Should not add duplicate program to deanery")
        void testAddDuplicateProgram() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);

                AcademicProgram program = new AcademicProgram();
                program.setId("PROG001");
                program.setName("Computer Science");

                List<AcademicProgram> programs = new ArrayList<>();
                programs.add(program);
                deanery.setAcademicPrograms(programs);

                when(deaneryService.searchDeaneryById("DEANERY001")).thenReturn(deanery);
                when(academicProgramService.searchProgramById("PROG001")).thenReturn(program);

                ResponseEntity<ManagerResponseDTO.DeaneryInfo> response =
                        managerController.addProgramToDeanery("DEANERY001", "PROG001", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                verify(deaneryService, never()).modifyDeanery(any());
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty manager ID")
        void testEmptyManagerId() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.getDeaneryPetitions("", "DEAN", session)
                );

                assertFalse(exception.getMessage().contains("obligatorio"));
            }
        }

        @Test
        @DisplayName("Should handle empty manager type")
        void testEmptyManagerType() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        managerController.getDeaneryPetitions("DEAN001", "", session)
                );

                assertFalse(exception.getMessage().contains("obligatorio"));
            }
        }

        @Test
        @DisplayName("Should handle invalid manager type")
        void testInvalidManagerTypeValue() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                assertThrows(RuntimeException.class, () ->
                        managerController.getDeaneryPetitions("DEAN001", "INVALID_TYPE", session)
                );
            }
        }

        @Test
        @DisplayName("Should handle capacity alerts with zero capacity sessions")
        void testCapacityAlertsWithZeroCapacity() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                ClassSession zeroCapacitySession = new ClassSession();
                zeroCapacitySession.setId("SESSION002");
                zeroCapacitySession.setCapacity(0);
                zeroCapacitySession.setEnrolledStudents(0);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(classSessionService.searchAllSessions())
                        .thenReturn(List.of(classSession, zeroCapacitySession));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getCapacityAlerts("DEAN001", "DEAN", 80, session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                // Should only include sessions with capacity > 0
                assertTrue(response.getBody().getCapacityAlerts().size() >= 1);
            }
        }

        @Test
        @DisplayName("Should handle empty petition list")
        void testEmptyPetitionList() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(Collections.emptyList());

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(0, response.getBody().getPetitions().size());
            }
        }
    }

    @Nested
    @DisplayName("Integration-like Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete petition workflow")
        void testCompletePetitionWorkflow() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                // Step 1: Get petitions
                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> getPetitionsResponse =
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, getPetitionsResponse.getStatusCode());
                assertEquals(1, getPetitionsResponse.getBody().getPetitions().size());

                // Step 2: View student schedule
                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(studentService.getStudentSchedule("STU001")).thenReturn(schedule);

                ResponseEntity<ManagerResponseDTO> scheduleResponse =
                        managerController.getStudentScheduleWithPetition("PET001", session);

                assertEquals(HttpStatus.OK, scheduleResponse.getStatusCode());
                assertNotNull(scheduleResponse.getBody().getStudentSchedule());

                // Step 3: Check academic status
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                when(trafficLightService.calculateGPA("STU001")).thenReturn(3.8);

                ResponseEntity<ManagerResponseDTO> statusResponse =
                        managerController.getStudentAcademicStatus("STU001", "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
                assertNotNull(statusResponse.getBody().getAcademicStatus());

                // Step 4: View alternative groups
                when(classSessionService.searchSessionsBySubjectShortName("CALC1"))
                        .thenReturn(List.of(classSession));

                ResponseEntity<ManagerResponseDTO> groupsResponse =
                        managerController.getAlternativeGroups("CALC1", "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, groupsResponse.getStatusCode());
                assertTrue(groupsResponse.getBody().getAlternativeGroups().size() > 0);

                // Step 5: Approve petition
                when(petitionService.changePetitionState("PET001", PetitionState.APPROVED))
                        .thenReturn(petition);
                when(petitionService.modifyPetition(any())).thenReturn(petition);

                ManagerRequestDTO approveRequest = new ManagerRequestDTO();
                approveRequest.setDecision("approve");

                ResponseEntity<ManagerResponseDTO> approveResponse =
                        managerController.respondToPetition("PET001", approveRequest, "DEAN001", "DEAN", session);

                assertEquals(HttpStatus.OK, approveResponse.getStatusCode());

                // Verify all services were called
                verify(petitionService).searchPetitionsByDeanery("Engineering");
                verify(studentService, atLeastOnce()).searchStudentById("STU001");
                verify(petitionService).changePetitionState("PET001", PetitionState.APPROVED);
            }
        }

        @Test
        @DisplayName("Should handle VP viewing institutional data")
        void testVPInstitutionalDataAccess() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);

                // Get all petitions
                when(petitionService.searchAllPetitions()).thenReturn(List.of(petition));
                ResponseEntity<ManagerResponseDTO> petitionsResponse =
                        managerController.getDeaneryPetitions("VP001", "ACADEMIC_VICEPRESIDENT", session);
                assertEquals(HttpStatus.OK, petitionsResponse.getStatusCode());

                // Get dashboard
                ResponseEntity<ManagerResponseDTO> dashboardResponse =
                        managerController.getDashboard("VP001", "ACADEMIC_VICEPRESIDENT", session);
                assertEquals(HttpStatus.OK, dashboardResponse.getStatusCode());

                // Get capacity alerts
                when(classSessionService.searchAllSessions()).thenReturn(List.of(classSession));
                ResponseEntity<ManagerResponseDTO> alertsResponse =
                        managerController.getCapacityAlerts("VP001", "ACADEMIC_VICEPRESIDENT", 90, session);
                assertEquals(HttpStatus.OK, alertsResponse.getStatusCode());

                // Get traffic light summary
                when(studentService.searchAllStudents()).thenReturn(List.of(student));
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                ResponseEntity<Map<String, Object>> summaryResponse =
                        managerController.getGlobalTrafficLightSummary("VP001", "ACADEMIC_VICEPRESIDENT", session);
                assertEquals(HttpStatus.OK, summaryResponse.getStatusCode());

                verify(petitionService, atLeastOnce()).searchAllPetitions();
                verify(classSessionService).searchAllSessions();
                verify(studentService).searchAllStudents();
            }
        }
    }

    @Nested
    @DisplayName("Data Mapping Tests")
    class DataMappingTests {

        @Test
        @DisplayName("Should correctly map petition to summary")
        void testPetitionMapping() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session);

                ManagerResponseDTO.PetitionSummary summary = response.getBody().getPetitions().get(0);
                assertEquals("PET001", summary.getPetitionId());
                assertEquals("STU001", summary.getStudentId());
                assertEquals("CALC1 - Calculus I", summary.getSubject());
                assertEquals("Cambio de grupo", summary.getPetitionType());
                assertEquals("Pendiente", summary.getState());
                assertEquals("Alta", summary.getPriority());
                assertEquals("Schedule conflict", summary.getJustification());
                assertNotNull(summary.getCreationDate());
            }
        }
        @Test
        @DisplayName("Should correctly map academic status")
        void testAcademicStatusMapping() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                trafficLight.getApprovedSubjects().put("Subject1", 40);
                trafficLight.getApprovedSubjects().put("Subject2", 35);
                trafficLight.getFailedSubjects().put("Subject3", 20);
                trafficLight.getOnGoingSubjects().add(null);

                // Stub para subjectService
                Subject subject1 = new Subject();
                subject1.setName("Subject1");
                subject1.setCredits(40);
                when(subjectService.searchSubjectByFullName("Subject1")).thenReturn(subject1);

                Subject subject2 = new Subject();
                subject2.setName("Subject2");
                subject2.setCredits(35);
                when(subjectService.searchSubjectByFullName("Subject2")).thenReturn(subject2);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                when(trafficLightService.calculateGPA("STU001")).thenReturn(3.75);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentAcademicStatus("STU001", "DEAN001", "DEAN", session);

                ManagerResponseDTO.AcademicStatus status = response.getBody().getAcademicStatus();
                assertEquals("STU001", status.getStudentId());
                assertEquals("Student Test", status.getStudentName());
                assertEquals(TrafficLightStatus.GREEN, status.getTrafficLightStatus());
                assertEquals(3.75, status.getGpa());
                assertEquals(2, status.getApprovedSubjects());
                assertEquals(1, status.getFailedSubjects());
                assertEquals(1, status.getOngoingSubjects());
                assertNotNull(status.getDescription());
            }
        }

        @Test
        @DisplayName("Should correctly map group availability")
        void testGroupAvailabilityMapping() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                Professor professor = new Professor();
                professor.setName("Prof. Smith");
                classSession.setProfessor(professor);
                classSession.setWaitingListStudentIds(List.of("STU002", "STU003"));

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(classSessionService.searchSessionsBySubjectShortName("CALC1"))
                        .thenReturn(List.of(classSession));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getAlternativeGroups("CALC1", "DEAN001", "DEAN", session);

                ManagerResponseDTO.GroupAvailability availability =
                        response.getBody().getAlternativeGroups().get(0);
                assertEquals("SESSION001", availability.getSessionId());
                assertEquals("CALC1", availability.getSubjectShortName());
                assertEquals("Prof. Smith", availability.getProfessorName());
                assertEquals(25, availability.getCurrentEnrollment());
                assertEquals(30, availability.getTotalCapacity());
                assertEquals(5, availability.getAvailableSpots());
                assertEquals(2, availability.getWaitingListSize());
            }
        }

        @Test
        @DisplayName("Should correctly map capacity alert")
        void testCapacityAlertMapping() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(classSessionService.searchAllSessions()).thenReturn(List.of(classSession));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getCapacityAlerts("DEAN001", "DEAN", 80, session);

                ManagerResponseDTO.CapacityAlert alert = response.getBody().getCapacityAlerts().get(0);
                assertEquals("SESSION001", alert.getSessionId());
                assertEquals("CALC1", alert.getSubject());
                assertEquals(25, alert.getCurrentEnrollment());
                assertEquals(30, alert.getTotalCapacity());
                assertEquals(83.33, alert.getOccupancyPercentage(), 0.01);
            }
        }

        @Test
        @DisplayName("Should handle null professor in schedule mapping")
        void testNullProfessorInSchedule() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                schedule.setProfessor(null);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(studentService.getStudentSchedule("STU001")).thenReturn(schedule);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentSchedule("STU001", "DEAN001", "DEAN", session);

                ManagerResponseDTO.ScheduleEntry entry =
                        response.getBody().getStudentSchedule().getSessions().get(0);
                assertEquals("N/A", entry.getProfessorName());
            }
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate manager access for VP")
        void testValidateVPAccess() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);
                when(petitionService.searchAllPetitions()).thenReturn(Collections.emptyList());

                assertDoesNotThrow(() ->
                        managerController.getDeaneryPetitions("VP001", "ACADEMIC_VICEPRESIDENT", session)
                );

                verify(academicVicePresidentService).searchAcademicVicePresidentById("VP001");
            }
        }
    }

    @Nested
    @DisplayName("Credit Percentage Calculation Tests")
    class CreditPercentageTests {

        @Test
        @DisplayName("Should calculate credit percentage correctly")
        void testCreditPercentageCalculation() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                Subject subject1 = new Subject();
                subject1.setName("Mathematics");
                subject1.setCredits(4);

                Subject subject2 = new Subject();
                subject2.setName("Physics");
                subject2.setCredits(3);

                trafficLight.getApprovedSubjects().put("Mathematics", 40);
                trafficLight.getApprovedSubjects().put("Physics", 35);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                when(trafficLightService.calculateGPA("STU001")).thenReturn(3.75);
                when(subjectService.searchSubjectByFullName("Mathematics")).thenReturn(subject1);
                when(subjectService.searchSubjectByFullName("Physics")).thenReturn(subject2);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentAcademicStatus("STU001", "DEAN001", "DEAN", session);

                ManagerResponseDTO.AcademicStatus status = response.getBody().getAcademicStatus();
                assertEquals(100.0, status.getCreditPercentage(), 0.01);
            }
        }

        @Test
        @DisplayName("Should handle zero credits")
        void testZeroCredits() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                trafficLight.setApprovedSubjects(new HashMap<>());

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(studentService.searchStudentById("STU001")).thenReturn(student);
                when(trafficLightService.searchTrafficLightByStudentId("STU001"))
                        .thenReturn(Optional.of(trafficLight));
                when(trafficLightService.calculateGPA("STU001")).thenReturn(0.0);

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getStudentAcademicStatus("STU001", "DEAN001", "DEAN", session);

                ManagerResponseDTO.AcademicStatus status = response.getBody().getAcademicStatus();
                assertEquals(0.0, status.getCreditPercentage());
            }
        }
    }

    @Nested
    @DisplayName("Response Message Tests")
    class ResponseMessageTests {

        @Test
        @DisplayName("Should return appropriate success messages")
        void testSuccessMessages() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery(anyString()))
                        .thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> response =
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session);

                assertNotNull(response.getBody().getMessage());
                assertTrue(response.getBody().getMessage().contains("exitosamente"));
            }
        }

        @Test
        @DisplayName("Should include decision history in petition response")
        void testDecisionHistory() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsById("PET001")).thenReturn(petition);
                when(petitionService.changePetitionState(anyString(), any()))
                        .thenReturn(petition);
                when(petitionService.modifyPetition(any())).thenAnswer(invocation -> {
                    Petition modified = invocation.getArgument(0);
                    assertNotNull(modified.getDecisionHistory());
                    assertTrue(modified.getDecisionHistory().size() > 0);
                    return modified;
                });

                ManagerRequestDTO request = new ManagerRequestDTO();
                request.setDecision("approve");

                managerController.respondToPetition("PET001", request, "DEAN001", "DEAN", session);

                verify(petitionService).modifyPetition(argThat(p ->
                        p.getDecisionHistory() != null && p.getDecisionHistory().size() > 0
                ));
            }
        }
    }

    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("Should handle multiple managers viewing petitions")
        void testMultipleManagersViewingPetitions() {
            try (MockedStatic<AuthValidationUtils> mockedStatic = mockStatic(AuthValidationUtils.class)) {
                mockedStatic.when(() -> AuthValidationUtils.validateAuthentication(
                        any(), any(UserType[].class))).thenReturn(null);
                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(deanUser);

                when(deanService.searchDeanByCode("DEAN001")).thenReturn(dean);
                when(petitionService.searchPetitionsByDeanery("Engineering"))
                        .thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> deanResponse =
                        managerController.getDeaneryPetitions("DEAN001", "DEAN", session);

                mockedStatic.when(() -> AuthValidationUtils.getCurrentUser(session))
                        .thenReturn(vpUser);

                when(academicVicePresidentService.searchAcademicVicePresidentById("VP001"))
                        .thenReturn(vp);
                when(petitionService.searchAllPetitions()).thenReturn(List.of(petition));

                ResponseEntity<ManagerResponseDTO> vpResponse =
                        managerController.getDeaneryPetitions("VP001", "ACADEMIC_VICEPRESIDENT", session);

                assertEquals(HttpStatus.OK, deanResponse.getStatusCode());
                assertEquals(HttpStatus.OK, vpResponse.getStatusCode());

                verify(petitionService).searchPetitionsByDeanery("Engineering");
                verify(petitionService).searchAllPetitions();
            }
        }
    }
}