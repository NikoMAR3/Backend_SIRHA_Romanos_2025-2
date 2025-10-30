package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
import edu.dosw.sirha.model.services.ClassSessionService.EnrollmentStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClassSessionService.
 * Validates all business logic for class session management including creation,
 * modification, scheduling, enrollments, and conflict detection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClassSessionService Tests")
class ClassSessionServiceTest {

    @Mock
    private ClassSessionRepository classSessionRepository;

    @InjectMocks
    private ClassSessionService classSessionService;

    private ClassSession testSession;
    private ClassSchedule testSchedule;
    private ClassSession conflictSession;
    private ClassSchedule conflictSchedule;

    /**
     * Sets up test data before each test execution.
     * Creates sample class sessions, schedules, and related test data.
     */
    @BeforeEach
    void setUp() {
        testSession = new ClassSession();
        testSession.setId("session123");
        testSession.setSubjectShortName("CALC1");
        testSession.setSubjectName("Cálculo I");
        testSession.setProfessorCode("prof123");
        testSession.setCapacity(30);
        testSession.setEnrolledStudents(15);
        testSession.setEnrolledStudentIds(new ArrayList<>(Arrays.asList("student1", "student2")));
        testSession.setWaitingListStudentIds(new ArrayList<>());

    
        testSchedule = new ClassSchedule();
        testSchedule.setId("schedule123");
        testSchedule.setDayOfWeek("Monday");
        testSchedule.setStartTime(LocalTime.of(8, 0));
        testSchedule.setEndTime(LocalTime.of(10, 0));
        testSchedule.setClassroom("A101");

        testSession.setSchedules(new ArrayList<>(Arrays.asList(testSchedule)));

        
        conflictSession = new ClassSession();
        conflictSession.setId("conflict123");
        conflictSession.setSubjectShortName("PHYS1");
        conflictSession.setSubjectName("Física I");
        conflictSession.setProfessorCode("prof456");
        conflictSession.setCapacity(25);
        conflictSession.setEnrolledStudents(10);


        conflictSchedule = new ClassSchedule();
        conflictSchedule.setId("conflictSchedule123");
        conflictSchedule.setDayOfWeek("Monday");
        conflictSchedule.setStartTime(LocalTime.of(9, 0));
        conflictSchedule.setEndTime(LocalTime.of(11, 0));
        conflictSchedule.setClassroom("A101");

        conflictSession.setSchedules(new ArrayList<>(Arrays.asList(conflictSchedule)));
    }

    @Nested
    @DisplayName("createClassSession() Tests")
    class CreateClassSessionTests {

        /**
         * Tests successful creation of a class session with valid data.
         */
        @Test
        @DisplayName("Should create class session successfully when valid session is provided")
        void createClassSession_ValidSession_ShouldReturnCreatedSession() {
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(new ArrayList<>());
            when(classSessionRepository.findAll()).thenReturn(new ArrayList<>());
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.createClassSession(testSession);

            assertNotNull(result);
            assertEquals(testSession.getId(), result.getId());
            assertEquals(testSession.getSubjectShortName(), result.getSubjectShortName());
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests exception handling when session is null.
         */
        @Test
        @DisplayName("Should throw exception when session is null")
        void createClassSession_NullSession_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.createClassSession(null)
            );

            assertEquals("Class session cannot be null", exception.getMessage());
            verify(classSessionRepository, never()).save(any());
        }

        /**
         * Tests validation of required fields.
         */
        @Test
        @DisplayName("Should throw exception when subject short name is null")
        void createClassSession_NullSubjectShortName_ShouldThrowException() {
            testSession.setSubjectShortName(null);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.createClassSession(testSession)
            );

            assertEquals("Subject short name cannot be null or empty", exception.getMessage());
            verify(classSessionRepository, never()).save(any());
        }

        /**
         * Tests validation of capacity.
         */
        @Test
        @DisplayName("Should throw exception when capacity is zero or negative")
        void createClassSession_InvalidCapacity_ShouldThrowException() {
            testSession.setCapacity(0);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.createClassSession(testSession)
            );

            assertEquals("Capacity must be greater than 0", exception.getMessage());
            verify(classSessionRepository, never()).save(any());
        }

        /**
         * Tests validation of enrolled students count.
         */
        @Test
        @DisplayName("Should throw exception when enrolled students exceeds capacity")
        void createClassSession_EnrolledExceedsCapacity_ShouldThrowException() {
            testSession.setEnrolledStudents(40);
            testSession.setCapacity(30);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.createClassSession(testSession)
            );

            assertEquals("Enrolled students cannot exceed capacity", exception.getMessage());
            verify(classSessionRepository, never()).save(any());
        }

        /**
         * Tests schedule conflict detection during creation.
         */
        @Test
        @DisplayName("Should throw exception when professor has schedule conflict")
        void createClassSession_ProfessorScheduleConflict_ShouldThrowException() {
            conflictSession.setProfessorCode("prof123");

            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(Arrays.asList(conflictSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.createClassSession(testSession)
            );

            assertTrue(exception.getMessage().contains("Professor prof123 has a time conflict"));

            verify(classSessionRepository).findByProfessorCode("prof123");
        }

        /**
         * Tests classroom conflict detection during creation.
         */
        @Test
        @DisplayName("Should throw exception when classroom has conflict")
        void createClassSession_ClassroomConflict_ShouldThrowException() {
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(new ArrayList<>());
            when(classSessionRepository.findAll()).thenReturn(Arrays.asList(conflictSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.createClassSession(testSession)
            );

            assertTrue(exception.getMessage().contains("Classroom A101 is already occupied"));
        }
    }

    @Nested
    @DisplayName("updateClassSession() Tests")
    class UpdateClassSessionTests {

        /**
         * Tests successful update of a class session.
         */
        @Test
        @DisplayName("Should update class session successfully when valid session is provided")
        void updateClassSession_ValidSession_ShouldReturnUpdatedSession() {
            when(classSessionRepository.existsById("session123")).thenReturn(true);
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(Arrays.asList(testSession));
            when(classSessionRepository.findAll()).thenReturn(Arrays.asList(testSession));
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.updateClassSession(testSession);

            assertNotNull(result);
            assertEquals(testSession.getId(), result.getId());
            verify(classSessionRepository).existsById("session123");
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests exception when session doesn't exist.
         */
        @Test
        @DisplayName("Should throw exception when session doesn't exist")
        void updateClassSession_NonExistentSession_ShouldThrowException() {
            when(classSessionRepository.existsById("session123")).thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.updateClassSession(testSession)
            );

            assertEquals("Class session with ID 'session123' not found", exception.getMessage());
            verify(classSessionRepository, never()).save(any());
        }

        /**
         * Tests exception when session or ID is null.
         */
        @Test
        @DisplayName("Should throw exception when session or ID is null")
        void updateClassSession_NullSessionOrId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.updateClassSession(null)
            );

            assertEquals("Class session and ID cannot be null", exception.getMessage());
            verify(classSessionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteClassSession() Tests")
    class DeleteClassSessionTests {

        /**
         * Tests successful deletion of a class session.
         */
        @Test
        @DisplayName("Should delete class session successfully when valid ID is provided")
        void deleteClassSession_ValidId_ShouldDeleteSuccessfully() {
            when(classSessionRepository.existsById("session123")).thenReturn(true);

            assertDoesNotThrow(() -> classSessionService.deleteClassSession("session123"));

            verify(classSessionRepository).existsById("session123");
            verify(classSessionRepository).deleteById("session123");
        }

        /**
         * Tests exception when ID is null or empty.
         */
        @Test
        @DisplayName("Should throw exception when ID is null or empty")
        void deleteClassSession_NullOrEmptyId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.deleteClassSession(null)
            );

            assertEquals("Session ID cannot be null or empty", exception.getMessage());
            verify(classSessionRepository, never()).deleteById(anyString());
        }

        /**
         * Tests exception when session doesn't exist.
         */
        @Test
        @DisplayName("Should throw exception when session doesn't exist")
        void deleteClassSession_NonExistentSession_ShouldThrowException() {
            when(classSessionRepository.existsById("session123")).thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.deleteClassSession("session123")
            );

            assertEquals("Class session with ID 'session123' not found", exception.getMessage());
            verify(classSessionRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("assignScheduleToSession() Tests")
    class AssignScheduleToSessionTests {

        /**
         * Tests successful schedule assignment.
         */
        @Test
        @DisplayName("Should assign schedule successfully when no conflicts exist")
        void assignScheduleToSession_NoConflicts_ShouldAssignSuccessfully() {
            ClassSchedule newSchedule = new ClassSchedule();
            newSchedule.setDayOfWeek("Tuesday");
            newSchedule.setStartTime(LocalTime.of(14, 0));
            newSchedule.setEndTime(LocalTime.of(16, 0));
            newSchedule.setClassroom("B201");

            testSession.setSchedules(new ArrayList<>());

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(Arrays.asList(testSession));
            when(classSessionRepository.findAll()).thenReturn(Arrays.asList(testSession));
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.assignScheduleToSession("session123", newSchedule);

            assertNotNull(result);
            assertEquals(1, testSession.getSchedules().size());
            assertTrue(testSession.getSchedules().contains(newSchedule));
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests exception when session ID is null.
         */
        @Test
        @DisplayName("Should throw exception when session ID is null")
        void assignScheduleToSession_NullSessionId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.assignScheduleToSession(null, testSchedule)
            );

            assertEquals("Session ID cannot be null or empty", exception.getMessage());
        }

        /**
         * Tests exception when schedule is null.
         */
        @Test
        @DisplayName("Should throw exception when schedule is null")
        void assignScheduleToSession_NullSchedule_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.assignScheduleToSession("session123", null)
            );

            assertEquals("Schedule cannot be null", exception.getMessage());
        }

        /**
         * Tests schedule validation.
         */
        @Test
        @DisplayName("Should throw exception when schedule has invalid times")
        void assignScheduleToSession_InvalidScheduleTimes_ShouldThrowException() {
            ClassSchedule invalidSchedule = new ClassSchedule();
            invalidSchedule.setDayOfWeek("Monday");
            invalidSchedule.setStartTime(LocalTime.of(16, 0));
            invalidSchedule.setEndTime(LocalTime.of(14, 0)); // End before start
            invalidSchedule.setClassroom("A101");

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.assignScheduleToSession("session123", invalidSchedule)
            );

            assertEquals("Start time must be before end time", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("searchSchedulesBySession() Tests")
    class SearchSchedulesBySessionTests {

        /**
         * Tests successful retrieval of schedules.
         */
        @Test
        @DisplayName("Should return schedules when session exists")
        void searchSchedulesBySession_ExistingSession_ShouldReturnSchedules() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            List<ClassSchedule> result = classSessionService.searchSchedulesBySession("session123");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testSchedule, result.get(0));
        }

        /**
         * Tests empty schedules list.
         */
        @Test
        @DisplayName("Should return empty list when session has no schedules")
        void searchSchedulesBySession_NoSchedules_ShouldReturnEmptyList() {
            testSession.setSchedules(null);
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            List<ClassSchedule> result = classSessionService.searchSchedulesBySession("session123");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * Tests exception when session doesn't exist.
         */
        @Test
        @DisplayName("Should throw exception when session doesn't exist")
        void searchSchedulesBySession_NonExistentSession_ShouldThrowException() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.searchSchedulesBySession("session123")
            );

            assertEquals("Class session with ID 'session123' not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("searchSessionsByProfessor() Tests")
    class SearchSessionsByProfessorTests {

        /**
         * Tests successful retrieval of professor sessions.
         */
        @Test
        @DisplayName("Should return sessions for valid professor ID")
        void searchSessionsByProfessor_ValidProfessorId_ShouldReturnSessions() {
            List<ClassSession> expectedSessions = Arrays.asList(testSession);
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(expectedSessions);

            List<ClassSession> result = classSessionService.searchSessionsByProfessor("prof123");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testSession, result.get(0));
            verify(classSessionRepository).findByProfessorCode("prof123");
        }

        /**
         * Tests exception when professor ID is null.
         */
        @Test
        @DisplayName("Should throw exception when professor ID is null")
        void searchSessionsByProfessor_NullProfessorId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.searchSessionsByProfessor(null)
            );

            assertEquals("Professor code cannot be null or empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("searchSessionsBySubjectShortName() Tests")
    class SearchSessionsBySubjectShortNameTests {

        /**
         * Tests successful retrieval of subject sessions.
         */
        @Test
        @DisplayName("Should return sessions for valid subject short name")
        void searchSessionsBySubjectShortName_ValidSubject_ShouldReturnSessions() {
            List<ClassSession> expectedSessions = Arrays.asList(testSession);
            when(classSessionRepository.findBySubjectShortName("CALC1")).thenReturn(expectedSessions);

            List<ClassSession> result = classSessionService.searchSessionsBySubjectShortName("CALC1");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testSession, result.get(0));
            verify(classSessionRepository).findBySubjectShortName("CALC1");
        }

        /**
         * Tests exception when subject short name is null.
         */
        @Test
        @DisplayName("Should throw exception when subject short name is null")
        void searchSessionsBySubjectShortName_NullSubject_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.searchSessionsBySubjectShortName(null)
            );

            assertEquals("Subject short name cannot be null or empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("checkScheduleConflicts() Tests")
    class CheckScheduleConflictsTests {

        /**
         * Tests conflict detection.
         */
        @Test
        @DisplayName("Should return true when conflicts exist")
        void checkScheduleConflicts_ConflictsExist_ShouldReturnTrue() {

            conflictSession.setProfessorCode("prof123");

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(Arrays.asList(testSession, conflictSession));

            boolean result = classSessionService.checkScheduleConflicts("session123");

            assertTrue(result);
            
            verify(classSessionRepository).findById("session123");
            verify(classSessionRepository).findByProfessorCode("prof123");
        }

        /**
         * Tests no conflicts scenario.
         */
        @Test
        @DisplayName("Should return false when no conflicts exist")
        void checkScheduleConflicts_NoConflicts_ShouldReturnFalse() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByProfessorCode("prof123")).thenReturn(Arrays.asList(testSession));
            when(classSessionRepository.findAll()).thenReturn(Arrays.asList(testSession));

            boolean result = classSessionService.checkScheduleConflicts("session123");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("enrollStudent() Tests")
    class EnrollStudentTests {

        /**
         * Tests successful student enrollment.
         */
        @Test
        @DisplayName("Should enroll student successfully when capacity is available")
        void enrollStudent_CapacityAvailable_ShouldEnrollSuccessfully() {
            testSession.setEnrolledStudents(15);
            testSession.setCapacity(30);
            testSession.setEnrolledStudentIds(new ArrayList<>(Arrays.asList("student1", "student2")));

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByEnrolledStudentId("student3")).thenReturn(new ArrayList<>());
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.enrollStudent("session123", "student3");

            assertNotNull(result);
            assertEquals(16, testSession.getEnrolledStudents());
            assertTrue(testSession.getEnrolledStudentIds().contains("student3"));
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests enrollment when session is at capacity (should add to waiting list).
         */
        @Test
        @DisplayName("Should add to waiting list when session is at capacity")
        void enrollStudent_AtCapacity_ShouldAddToWaitingList() {
            testSession.setEnrolledStudents(30);
            testSession.setCapacity(30);

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByEnrolledStudentId("student3")).thenReturn(new ArrayList<>());
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.enrollStudent("session123", "student3");

            assertNotNull(result);
            assertTrue(testSession.getWaitingListStudentIds().contains("student3"));
            assertFalse(testSession.getEnrolledStudentIds().contains("student3"));
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests exception when student is already enrolled.
         */
        @Test
        @DisplayName("Should throw exception when student is already enrolled")
        void enrollStudent_AlreadyEnrolled_ShouldThrowException() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.enrollStudent("session123", "student1")
            );

            // Cambiar el mensaje esperado para que coincida con el actual del servicio
            assertEquals("Student is already enrolled", exception.getMessage());
        }

        /**
         * Tests schedule conflict detection during enrollment.
         */
        @Test
        @DisplayName("Should throw exception when student has schedule conflict")
        void enrollStudent_ScheduleConflict_ShouldThrowException() {
            ClassSession studentSession = new ClassSession();
            studentSession.setSubjectShortName("PHYS1");
            studentSession.setSchedules(Arrays.asList(testSchedule)); // Mismo horario

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByEnrolledStudentId("student3")).thenReturn(Arrays.asList(studentSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.enrollStudent("session123", "student3")
            );

            assertTrue(exception.getMessage().contains("Schedule conflict"));
        }
    }

    @Nested
    @DisplayName("withdrawStudent() Tests")
    class WithdrawStudentTests {

        /**
         * Tests successful student withdrawal.
         */
        @Test
        @DisplayName("Should withdraw student successfully and promote from waiting list")
        void withdrawStudent_WithWaitingList_ShouldWithdrawAndPromote() {
            testSession.setWaitingListStudentIds(new ArrayList<>(Arrays.asList("waitingStudent1")));

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.withdrawStudent("session123", "student1");

            assertNotNull(result);
            assertFalse(testSession.getEnrolledStudentIds().contains("student1"));
            assertTrue(testSession.getEnrolledStudentIds().contains("waitingStudent1"));
            assertEquals(15, testSession.getEnrolledStudents()); // Mismo número por promoción
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests withdrawal when student is not enrolled.
         */
        @Test
        @DisplayName("Should throw exception when student is not enrolled")
        void withdrawStudent_NotEnrolled_ShouldThrowException() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.withdrawStudent("session123", "student999")
            );

            assertEquals("Student 'student999' is not enrolled in this session", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("getStudentEnrollments() Tests")
    class GetStudentEnrollmentsTests {

        /**
         * Tests retrieval of student enrollments.
         */
        @Test
        @DisplayName("Should return student enrollments when valid student ID is provided")
        void getStudentEnrollments_ValidStudentId_ShouldReturnEnrollments() {
            List<ClassSession> expectedSessions = Arrays.asList(testSession);
            when(classSessionRepository.findByEnrolledStudentId("student1")).thenReturn(expectedSessions);

            List<ClassSession> result = classSessionService.getStudentEnrollments("student1");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testSession, result.get(0));
            verify(classSessionRepository).findByEnrolledStudentId("student1");
        }

        /**
         * Tests exception when student ID is null.
         */
        @Test
        @DisplayName("Should throw exception when student ID is null")
        void getStudentEnrollments_NullStudentId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.getStudentEnrollments(null)
            );

            assertEquals("Student ID cannot be null or empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("getStudentWaitingList() Tests")
    class GetStudentWaitingListTests {

        /**
         * Tests retrieval of student waiting list.
         */
        @Test
        @DisplayName("Should return waiting list sessions when valid student ID is provided")
        void getStudentWaitingList_ValidStudentId_ShouldReturnWaitingList() {
            List<ClassSession> expectedSessions = Arrays.asList(testSession);
            when(classSessionRepository.findByWaitingListStudentId("student1")).thenReturn(expectedSessions);

            List<ClassSession> result = classSessionService.getStudentWaitingList("student1");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testSession, result.get(0));
            verify(classSessionRepository).findByWaitingListStudentId("student1");
        }
    }

    @Nested
    @DisplayName("canStudentEnroll() Tests")
    class CanStudentEnrollTests {

        /**
         * Tests enrollment eligibility check.
         */
        @Test
        @DisplayName("Should return true when student can enroll")
        void canStudentEnroll_EligibleStudent_ShouldReturnTrue() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.findByEnrolledStudentId("student3")).thenReturn(new ArrayList<>());

            boolean result = classSessionService.canStudentEnroll("session123", "student3");

            assertTrue(result);
        }

        /**
         * Tests enrollment eligibility when student is already enrolled.
         */
        @Test
        @DisplayName("Should return false when student is already enrolled")
        void canStudentEnroll_AlreadyEnrolled_ShouldReturnFalse() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            boolean result = classSessionService.canStudentEnroll("session123", "student1");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getEnrollmentStats() Tests")
    class GetEnrollmentStatsTests {

        /**
         * Tests enrollment statistics calculation.
         */
        @Test
        @DisplayName("Should return correct enrollment statistics")
        void getEnrollmentStats_ValidSession_ShouldReturnCorrectStats() {
            testSession.setWaitingListStudentIds(Arrays.asList("waiting1", "waiting2"));

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            EnrollmentStats result = classSessionService.getEnrollmentStats("session123");

            assertNotNull(result);
            assertEquals(15, result.getEnrolled());
            assertEquals(30, result.getCapacity());
            assertEquals(2, result.getWaitingList());
            assertEquals(15, result.getAvailable());
            assertEquals(50.0, result.getOccupancyPercentage(), 0.01);
        }

        /**
         * Tests enrollment statistics with null waiting list.
         */
        @Test
        @DisplayName("Should handle null waiting list in statistics")
        void getEnrollmentStats_NullWaitingList_ShouldHandleGracefully() {
            testSession.setWaitingListStudentIds(null);

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            EnrollmentStats result = classSessionService.getEnrollmentStats("session123");

            assertNotNull(result);
            assertEquals(0, result.getWaitingList());
        }
    }

    @Nested
    @DisplayName("promoteFromWaitingList() Tests")
    class PromoteFromWaitingListTests {

        /**
         * Tests successful promotion from waiting list.
         */
        @Test
        @DisplayName("Should promote student from waiting list when capacity allows")
        void promoteFromWaitingList_CapacityAvailable_ShouldPromoteSuccessfully() {
            testSession.setEnrolledStudents(20);
            testSession.setCapacity(30);
            testSession.setWaitingListStudentIds(new ArrayList<>(Arrays.asList("waitingStudent1")));

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));
            when(classSessionRepository.save(any(ClassSession.class))).thenReturn(testSession);

            ClassSession result = classSessionService.promoteFromWaitingList("session123", "waitingStudent1");

            assertNotNull(result);
            assertFalse(testSession.getWaitingListStudentIds().contains("waitingStudent1"));
            assertTrue(testSession.getEnrolledStudentIds().contains("waitingStudent1"));
            assertEquals(21, testSession.getEnrolledStudents());
            verify(classSessionRepository).save(testSession);
        }

        /**
         * Tests promotion when session is at capacity.
         */
        @Test
        @DisplayName("Should throw exception when session is at full capacity")
        void promoteFromWaitingList_AtCapacity_ShouldThrowException() {
            testSession.setEnrolledStudents(30);
            testSession.setCapacity(30);
            testSession.setWaitingListStudentIds(Arrays.asList("waitingStudent1"));

            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.promoteFromWaitingList("session123", "waitingStudent1")
            );

            assertEquals("Session is at full capacity", exception.getMessage());
        }

        /**
         * Tests promotion when student is not on waiting list.
         */
        @Test
        @DisplayName("Should throw exception when student is not on waiting list")
        void promoteFromWaitingList_NotOnWaitingList_ShouldThrowException() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classSessionService.promoteFromWaitingList("session123", "student999")
            );

            assertEquals("Student is not on waiting list", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("EnrollmentStats Tests")
    class EnrollmentStatsTests {

        /**
         * Tests EnrollmentStats creation and getters.
         */
        @Test
        @DisplayName("Should create EnrollmentStats with correct values")
        void enrollmentStats_ShouldCreateWithCorrectValues() {
            EnrollmentStats stats = new EnrollmentStats(20, 30, 5, 10);

            assertEquals(20, stats.getEnrolled());
            assertEquals(30, stats.getCapacity());
            assertEquals(5, stats.getWaitingList());
            assertEquals(10, stats.getAvailable());
            assertEquals(66.67, stats.getOccupancyPercentage(), 0.01);
        }

        /**
         * Tests occupancy percentage calculation with zero capacity.
         */
        @Test
        @DisplayName("Should handle zero capacity in occupancy percentage")
        void enrollmentStats_ZeroCapacity_ShouldReturnZeroPercentage() {
            EnrollmentStats stats = new EnrollmentStats(0, 0, 0, 0);

            assertEquals(0.0, stats.getOccupancyPercentage(), 0.01);
        }
    }
}