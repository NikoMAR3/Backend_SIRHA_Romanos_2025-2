package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
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
        mongoTemplate.dropCollection(ClassSession.class);
        mongoTemplate.dropCollection(Professor.class);
        mongoTemplate.dropCollection(ClassSchedule.class);

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

        schedule1 = new ClassSchedule("schedule1", "MONDAY", LocalTime.parse("08:00"), LocalTime.parse("10:00"), "A101");
        schedule2 = new ClassSchedule("schedule2", "WEDNESDAY", LocalTime.parse("10:00"), LocalTime.parse("12:00"), "A102");
        schedule3 = new ClassSchedule("schedule3", "FRIDAY", LocalTime.parse("14:00"), LocalTime.parse("16:00"), "A101");
        mongoTemplate.save(schedule1);
        mongoTemplate.save(schedule2);
        mongoTemplate.save(schedule3);

        session1 = new ClassSession();
        session1.setSubjectShortName("MATH101");
        session1.setSubjectName("Mathematics I");
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
        session2.setCapacity(25);
        session2.setEnrolledStudents(25);
        session2.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session2.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session2.setProfessor(professor1);
        session2.setSchedules(Arrays.asList(schedule3));
        session2.setEnrolledStudentIds(Arrays.asList("student6", "student7"));

        session3 = new ClassSession();
        session3.setSubjectShortName("MATH101");
        session3.setSubjectName("Mathematics I");
        session3.setCapacity(20);
        session3.setEnrolledStudents(15);
        session3.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session3.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session3.setProfessor(professor2);
        session3.setSchedules(Arrays.asList(schedule1));
        session3.setEnrolledStudentIds(Arrays.asList("student8", "student9"));

        session4 = new ClassSession();
        session4.setSubjectShortName("CHEM101");
        session4.setSubjectName("Chemistry I");
        session4.setCapacity(30);
        session4.setEnrolledStudents(30);
        session4.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        session4.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        session4.setProfessor(professor2);
        session4.setSchedules(Arrays.asList(schedule2));
        session4.setEnrolledStudentIds(Arrays.asList("student10", "student11", "student12"));
        session4.setWaitingListStudentIds(Arrays.asList("student13"));

        classSessionRepository.saveAll(Arrays.asList(session1, session2, session3, session4));
    }

    @Test
    void findById_ShouldReturnClassSession_WhenIdExists() {
        Optional<ClassSession> result = classSessionRepository.findById(session1.getId());
        assertTrue(result.isPresent());
        assertEquals("MATH101", result.get().getSubjectShortName());
        assertEquals("Mathematics I", result.get().getSubjectName());
        assertEquals(professor1.getId(), result.get().getProfessor().getId());
    }

    @Test
    void findByProfessor_ShouldReturnClassSessions_WhenProfessorExists() {
        List<ClassSession> result = classSessionRepository.findByProfessor(professor1);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassSession::getSubjectShortName)
                .containsExactlyInAnyOrder("MATH101", "PHY101");
    }

    @Test
    void findBySubjectShortName_ShouldReturnClassSessions_WhenSubjectExists() {
        List<ClassSession> result = classSessionRepository.findBySubjectShortName("MATH101");
        assertThat(result).hasSize(2);
        assertThat(result).extracting(s -> s.getProfessor().getId())
                .containsExactlyInAnyOrder(professor1.getId(), professor2.getId());
    }

    @Test
    void findSessionsWithAvailableCapacity_ShouldReturnSessionsWithAvailableSpots() {
        List<ClassSession> result = classSessionRepository.findSessionsWithAvailableCapacity();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getEnrolledStudents() < s.getCapacity());
    }

    @Test
    void save_ShouldPersistClassSessionWithAllRelations() {
        ClassSession newSession = new ClassSession();
        newSession.setSubjectShortName("BIO101");
        newSession.setSubjectName("Biology I");
        newSession.setCapacity(40);
        newSession.setEnrolledStudents(35);
        newSession.setStartDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        newSession.setEndDate(LocalDateTime.of(2024, 5, 15, 0, 0));
        newSession.setProfessor(professor2);
        newSession.setSchedules(Arrays.asList(schedule1, schedule3));
        newSession.setEnrolledStudentIds(Arrays.asList("student14", "student15"));
        newSession.setWaitingListStudentIds(Arrays.asList("student16"));

        ClassSession saved = classSessionRepository.save(newSession);
        assertNotNull(saved.getId());
        assertEquals(professor2.getId(), saved.getProfessor().getId());
    }

}
