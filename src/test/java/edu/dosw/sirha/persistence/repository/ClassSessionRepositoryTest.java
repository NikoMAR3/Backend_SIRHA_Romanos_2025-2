package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class ClassSessionRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Professor professor1, professor2;
    private ClassSchedule schedule1, schedule2, schedule3;
    private ClassSession session1, session2, session3, session4;

    @BeforeEach
    void setUp() {
        // Limpiar las colecciones antes de cada test
        mongoTemplate.dropCollection(ClassSession.class);
        mongoTemplate.dropCollection(Professor.class);
        mongoTemplate.dropCollection(ClassSchedule.class);

        // Crear y guardar profesores
        professor1 = new Professor();
        professor1.setId("prof1");
        professor1.setName("Dr. Juan Pérez");
        professor1.setMail("juan.perez@university.edu");
        professor1.setDocument("12345678");
        mongoTemplate.save(professor1);

        professor2 = new Professor();
        professor2.setId("prof2");
        professor2.setName("Dra. María García");
        professor2.setMail("maria.garcia@university.edu");
        professor2.setDocument("87654321");
        mongoTemplate.save(professor2);

        // Crear y guardar horarios
        schedule1 = new ClassSchedule();
        schedule1.setId("schedule1");
        schedule1.setDayOfWeek("MONDAY");
        schedule1.setStartTime(LocalTime.parse("08:00"));
        schedule1.setEndTime(LocalTime.parse("10:00"));
        schedule1.setClassroom("A101");
        mongoTemplate.save(schedule1);

        schedule2 = new ClassSchedule();
        schedule2.setId("schedule2");
        schedule2.setDayOfWeek("WEDNESDAY");
        schedule2.setStartTime(LocalTime.parse("10:00"));
        schedule2.setEndTime(LocalTime.parse("12:00"));
        schedule2.setClassroom("A102");
        mongoTemplate.save(schedule2);

        schedule3 = new ClassSchedule();
        schedule3.setId("schedule3");
        schedule3.setDayOfWeek("FRIDAY");
        schedule3.setStartTime(LocalTime.parse("14:00"));
        schedule3.setEndTime(LocalTime.parse("16:00"));
        schedule3.setClassroom("A101");
        mongoTemplate.save(schedule3);

        // Crear sesiones de clase
        session1 = new ClassSession();
        session1.setSubjectShortName("MATH101");
        session1.setSubjectName("Mathematics I");
        session1.setProfessorId("prof1");
        session1.setCapacity(30);
        session1.setEnrolledStudents(25);
        session1.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session1.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session1.setProfessor(professor1);
        session1.setSchedules(Arrays.asList(schedule1, schedule2));
        session1.setEnrolledStudentIds(Arrays.asList("student1", "student2", "student3"));
        session1.setWaitingListStudentIds(Arrays.asList("student4", "student5"));

        session2 = new ClassSession();
        session2.setSubjectShortName("PHY101");
        session2.setSubjectName("Physics I");
        session2.setProfessorId("prof1");
        session2.setCapacity(25);
        session2.setEnrolledStudents(25);
        session2.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session2.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session2.setProfessor(professor1);
        session2.setSchedules(Arrays.asList(schedule3));
        session2.setEnrolledStudentIds(Arrays.asList("student6", "student7"));
        session2.setWaitingListStudentIds(Arrays.asList());

        session3 = new ClassSession();
        session3.setSubjectShortName("MATH101");
        session3.setSubjectName("Mathematics I");
        session3.setProfessorId("prof2");
        session3.setCapacity(20);
        session3.setEnrolledStudents(15);
        session3.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session3.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session3.setProfessor(professor2);
        session3.setSchedules(Arrays.asList(schedule1)); // Mismo horario que session1 pero diferente profesor
        session3.setEnrolledStudentIds(Arrays.asList("student8", "student9"));
        session3.setWaitingListStudentIds(Arrays.asList());

        session4 = new ClassSession();
        session4.setSubjectShortName("CHEM101");
        session4.setSubjectName("Chemistry I");
        session4.setProfessorId("prof2");
        session4.setCapacity(30);
        session4.setEnrolledStudents(30);
        session4.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session4.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session4.setProfessor(professor2);
        session4.setSchedules(Arrays.asList(schedule2));
        session4.setEnrolledStudentIds(Arrays.asList("student10", "student11", "student12"));
        session4.setWaitingListStudentIds(Arrays.asList("student13"));

        // Guardar sesiones de clase
        classSessionRepository.saveAll(Arrays.asList(session1, session2, session3, session4));
    }

    @Test
    void findById_ShouldReturnClassSession_WhenIdExists() {
        // Given
        String sessionId = session1.getId();

        // When
        Optional<ClassSession> result = classSessionRepository.findById(sessionId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionId, result.get().getId());
        assertEquals("MATH101", result.get().getSubjectShortName());
        assertEquals("Mathematics I", result.get().getSubjectName());
        assertEquals("prof1", result.get().getProfessorId());
        assertEquals(30, result.get().getCapacity());
        assertEquals(25, result.get().getEnrolledStudents());
        assertEquals(2, result.get().getSchedules().size());
        assertEquals(3, result.get().getEnrolledStudentIds().size());
        assertEquals(2, result.get().getWaitingListStudentIds().size());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<ClassSession> result = classSessionRepository.findById("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByProfessorId_ShouldReturnClassSessions_WhenProfessorExists() {
        // When
        List<ClassSession> result = classSessionRepository.findByProfessorId("prof1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassSession::getSubjectShortName)
                .containsExactlyInAnyOrder("MATH101", "PHY101");
    }

    @Test
    void findByProfessorId_ShouldReturnEmptyList_WhenProfessorDoesNotExist() {
        // When
        List<ClassSession> result = classSessionRepository.findByProfessorId("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findBySubjectShortName_ShouldReturnClassSessions_WhenSubjectExists() {
        // When
        List<ClassSession> result = classSessionRepository.findBySubjectShortName("MATH101");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassSession::getProfessorId)
                .containsExactlyInAnyOrder("prof1", "prof2");
    }

    @Test
    void findBySubjectShortName_ShouldReturnEmptyList_WhenSubjectDoesNotExist() {
        // When
        List<ClassSession> result = classSessionRepository.findBySubjectShortName("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsScheduleConflicts_ShouldReturnTrue_WhenConflictExists() {
        // Given: session1 has schedule1 (MONDAY 08:00-10:00) and schedule2
        // We are checking for a new session that would conflict with session1 in the same classroom and time

        // When: Check for conflict with the same classroom and time
        boolean conflict = classSessionRepository.existsScheduleConflicts(
                "nonexistent", // ID of a new session (so it doesn't match session1's ID)
                "MONDAY",
                LocalTime.parse("08:00"),
                LocalTime.parse("10:00"),
                "A101",
                "prof2" // Different professor, but same classroom and time -> conflict
        );

        // Then
        assertTrue(conflict);
    }

    @Test
    void existsScheduleConflicts_ShouldReturnFalse_WhenNoConflict() {
        boolean conflict = classSessionRepository.existsScheduleConflicts(
                "nonexistent",
                "MONDAY",
                LocalTime.parse("11:00"),
                LocalTime.parse("13:00"),
                "A101",
                "prof1"
        );
        // Then
        assertFalse(conflict);
    }

    @Test
    void findSessionsWithAvailableCapacity_ShouldReturnSessionsWithAvailableSpots() {
        // When
        List<ClassSession> result = classSessionRepository.findSessionsWithAvailableCapacity();

        // Then: session1 (25/30) and session3 (15/20) have available capacity, session2 (25/25) and session4 (30/30) are full
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassSession::getSubjectShortName)
                .containsExactlyInAnyOrder("MATH101", "MATH101");
        // Verify that the sessions with available capacity are the ones we expect
        assertThat(result).allMatch(session -> session.getEnrolledStudents() < session.getCapacity());
    }

    @Test
    void findBySubjectName_ShouldReturnClassSessions_WhenSubjectNameExists() {
        // When
        List<ClassSession> result = classSessionRepository.findBySubjectName("Mathematics I");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassSession::getSubjectShortName)
                .containsExactly("MATH101", "MATH101");
    }

    @Test
    void findBySubjectName_ShouldReturnEmptyList_WhenSubjectNameDoesNotExist() {
        // When
        List<ClassSession> result = classSessionRepository.findBySubjectName("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEnrolledStudentId_ShouldReturnClassSessions_WhenStudentIsEnrolled() {
        // When
        List<ClassSession> result = classSessionRepository.findByEnrolledStudentId("student1");

        // Then: student1 is enrolled in session1
        assertThat(result).hasSize(1);
        assertEquals("MATH101", result.get(0).getSubjectShortName());
    }

    @Test
    void findByEnrolledStudentId_ShouldReturnEmptyList_WhenStudentIsNotEnrolled() {
        // When
        List<ClassSession> result = classSessionRepository.findByEnrolledStudentId("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByWaitingListStudentId_ShouldReturnClassSessions_WhenStudentIsOnWaitingList() {
        // When
        List<ClassSession> result = classSessionRepository.findByWaitingListStudentId("student4");

        // Then: student4 is on waiting list of session1
        assertThat(result).hasSize(1);
        assertEquals("MATH101", result.get(0).getSubjectShortName());
    }

    @Test
    void findByWaitingListStudentId_ShouldReturnEmptyList_WhenStudentIsNotOnWaitingList() {
        // When
        List<ClassSession> result = classSessionRepository.findByWaitingListStudentId("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByIdAndEnrolledStudentId_ShouldReturnTrue_WhenStudentIsEnrolledInSessionAndEnrolledStudentIdsContains() {
        String sessionId = session1.getId();
        String studentId = "student1";
        boolean exists = classSessionRepository.existsByIdAndEnrolledStudentIdsContains(sessionId, studentId);
        assertTrue(exists);
    }

    @Test
    void existsByIdAndEnrolledStudentId_ShouldReturnFalse_WhenStudentIsNotEnrolledInSessionAndEnrolledStudentIdsContains() {
        // Given
        String sessionId = session1.getId();
        String studentId = "nonexistent";

        // When
        boolean exists = classSessionRepository.existsByIdAndEnrolledStudentIdsContains(sessionId, studentId);

        // Then
        assertFalse(exists);
    }

    @Test
    void findBySubjectShortNameWithAvailableCapacity_ShouldReturnSessionsWithAvailableSpotsForSubject() {
        // When
        List<ClassSession> result = classSessionRepository.findBySubjectShortNameWithAvailableCapacity("MATH101");

        // Then: session1 and session3 are MATH101, but only session1 and session3 have available capacity
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassSession::getSubjectShortName)
                .containsExactly("MATH101", "MATH101");
        assertThat(result).allMatch(session -> session.getEnrolledStudents() < session.getCapacity());
    }

    @Test
    void findBySubjectShortNameWithAvailableCapacity_ShouldReturnEmptyList_WhenNoSessionsWithAvailableCapacity() {
        // When: We look for PHY101, but session2 is full
        List<ClassSession> result = classSessionRepository.findBySubjectShortNameWithAvailableCapacity("PHY101");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistClassSessionWithAllRelations() {
        // Given
        ClassSession newSession = new ClassSession();
        newSession.setSubjectShortName("BIO101");
        newSession.setSubjectName("Biology I");
        newSession.setProfessorId("prof2");
        newSession.setCapacity(40);
        newSession.setEnrolledStudents(35);
        newSession.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        newSession.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        newSession.setProfessor(professor2);
        newSession.setSchedules(Arrays.asList(schedule1, schedule3));
        newSession.setEnrolledStudentIds(Arrays.asList("student14", "student15"));
        newSession.setWaitingListStudentIds(Arrays.asList("student16"));

        // When
        ClassSession savedSession = classSessionRepository.save(newSession);

        // Then
        assertNotNull(savedSession.getId());
        assertEquals("BIO101", savedSession.getSubjectShortName());
        assertEquals("Biology I", savedSession.getSubjectName());
        assertEquals("prof2", savedSession.getProfessorId());
        assertEquals(40, savedSession.getCapacity());
        assertEquals(35, savedSession.getEnrolledStudents());
        assertEquals(2, savedSession.getSchedules().size());
        assertEquals(2, savedSession.getEnrolledStudentIds().size());
        assertEquals(1, savedSession.getWaitingListStudentIds().size());

        // Verify it can be retrieved
        Optional<ClassSession> retrievedSession = classSessionRepository.findById(savedSession.getId());
        assertTrue(retrievedSession.isPresent());
    }

    @Test
    void delete_ShouldRemoveClassSession() {
        // Given
        String sessionId = session1.getId();

        // When
        classSessionRepository.deleteById(sessionId);

        // Then
        Optional<ClassSession> result = classSessionRepository.findById(sessionId);
        assertFalse(result.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectNumberOfClassSessions() {
        // When
        long count = classSessionRepository.count();

        // Then
        assertEquals(4, count);
    }

    @Test
    void existsById_ShouldReturnTrueForExistingClassSession() {
        // When
        boolean exists = classSessionRepository.existsById(session1.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingClassSession() {
        // When
        boolean exists = classSessionRepository.existsById("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldHandleComplexScheduleConflictsScenarios() {
        // Test 1: Conflict with same professor, same time, different classroom
        boolean conflict1 = classSessionRepository.existsScheduleConflicts(
                "newSession1",
                "MONDAY",
                LocalTime.parse("08:00"),
                LocalTime.parse("10:00"),
                "B201", // Different classroom
                "prof1" // Same professor -> conflict
        );
        assertTrue(conflict1);

        // Test 2: No conflict - different day
        boolean conflict2 = classSessionRepository.existsScheduleConflicts(
                "newSession2",
                "TUESDAY", // Different day
                LocalTime.parse("08:00"),
                LocalTime.parse("10:00"),
                "A101",
                "prof2"
        );
        assertFalse(conflict2);

        // Test 3: No conflict - different time range
        boolean conflict3 = classSessionRepository.existsScheduleConflicts(
                "newSession3",
                "MONDAY",
                LocalTime.parse("07:00"),
                LocalTime.parse("07:59"),
                "A101",
                "prof2"
        );
        assertFalse(conflict3);
    }

    @Test
    void shouldHandleMultipleStudentsInMultipleSessions() {
        // When: Check for student enrolled in multiple sessions
        List<ClassSession> student1Sessions = classSessionRepository.findByEnrolledStudentId("student1");
        List<ClassSession> student8Sessions = classSessionRepository.findByEnrolledStudentId("student8");

        // Then
        assertThat(student1Sessions).hasSize(1); // student1 only in session1
        assertThat(student8Sessions).hasSize(1); // student8 only in session3
    }

    @Test
    void shouldHandleEmptyAndNullFieldsGracefully() {
        // Given: Create a session with minimal data
        ClassSession minimalSession = new ClassSession();
        minimalSession.setSubjectShortName("TEST101");
        minimalSession.setSubjectName("Test Subject");
        minimalSession.setProfessorId("prof1");
        minimalSession.setCapacity(10);
        minimalSession.setEnrolledStudents(0);

        // When
        ClassSession savedSession = classSessionRepository.save(minimalSession);

        // Then
        assertNotNull(savedSession.getId());
        assertEquals("TEST101", savedSession.getSubjectShortName());
        assertNull(savedSession.getSchedules()); // Should be null
        assertNull(savedSession.getEnrolledStudentIds()); // Should be null
        assertNull(savedSession.getWaitingListStudentIds()); // Should be null
    }
}