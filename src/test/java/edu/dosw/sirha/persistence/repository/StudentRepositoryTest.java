package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.persistence.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class StudentRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private TrafficLight trafficLight1, trafficLight2;
    private Schedule schedule1, schedule2;
    private AcademicProgram academicProgram1, academicProgram2;
    private Deanery deanery1, deanery2;
    private Student student1, student2, student3, student4;

    @BeforeEach
    void setUp() {
        // Limpiar las colecciones antes de cada test
        mongoTemplate.dropCollection(Student.class);
        mongoTemplate.dropCollection(TrafficLight.class);
        mongoTemplate.dropCollection(Schedule.class);
        mongoTemplate.dropCollection(AcademicProgram.class);
        mongoTemplate.dropCollection(Deanery.class);

        // Crear y guardar TrafficLights
        trafficLight1 = new TrafficLight();
        trafficLight1.setId("traffic1");
        mongoTemplate.save(trafficLight1);

        trafficLight2 = new TrafficLight();
        trafficLight2.setId("traffic2");
        mongoTemplate.save(trafficLight2);

        // Crear y guardar Schedules
        schedule1 = new Schedule();
        schedule1.setId("schedule1");
        mongoTemplate.save(schedule1);

        schedule2 = new Schedule();
        schedule2.setId("schedule2");
        mongoTemplate.save(schedule2);

        // Crear y guardar AcademicPrograms
        academicProgram1 = new AcademicProgram();
        academicProgram1.setId("program1");
        academicProgram1.setName("Computer Science");
        mongoTemplate.save(academicProgram1);

        academicProgram2 = new AcademicProgram();
        academicProgram2.setId("program2");
        academicProgram2.setName("Electrical Engineering");
        mongoTemplate.save(academicProgram2);

        // Crear y guardar Deaneries
        deanery1 = new Deanery();
        deanery1.setId("deanery1");
        deanery1.setDeaneryName("Faculty of Engineering");
        mongoTemplate.save(deanery1);

        deanery2 = new Deanery();
        deanery2.setId("deanery2");
        deanery2.setDeaneryName("Faculty of Sciences");
        mongoTemplate.save(deanery2);

        // Crear estudiantes
        student1 = new Student("ST001", "Juan Pérez", "juan.perez@university.edu", "12345678");
        student1.setTrafficLight(trafficLight1);
        student1.setSchedules(Arrays.asList(schedule1));
        student1.setAcademicProgram(academicProgram1);
        student1.setAcademicStatus(AcademicStatus.ACTIVE);
        student1.setDeanery(deanery1);
        student1.setSemester(5);

        student2 = new Student("ST002", "María García", "maria.garcia@university.edu", "87654321");
        student2.setTrafficLight(trafficLight2);
        student2.setSchedules(Arrays.asList(schedule2));
        student2.setAcademicProgram(academicProgram2);
        student2.setAcademicStatus(AcademicStatus.SUSPENDED);
        student2.setDeanery(deanery2);
        student2.setSemester(3);

        student3 = new Student("ST003", "Carlos Rodríguez", "carlos.rodriguez@university.edu", "11223344");
        student3.setTrafficLight(trafficLight1);
        student3.setSchedules(Arrays.asList(schedule1, schedule2));
        student3.setAcademicProgram(academicProgram1);
        student3.setAcademicStatus(AcademicStatus.ACTIVE);
        student3.setDeanery(deanery1);
        student3.setSemester(5);

        student4 = new Student("ST004", "Ana López", "ana.lopez@university.edu", "44332211");
        student4.setTrafficLight(trafficLight2);
        student4.setSchedules(Arrays.asList(schedule1));
        student4.setAcademicProgram(academicProgram1);
        student4.setAcademicStatus(AcademicStatus.INACTIVE);
        student4.setDeanery(deanery1);
        student4.setSemester(2);

        // Guardar estudiantes
        studentRepository.saveAll(Arrays.asList(student1, student2, student3, student4));
    }

    // --- TESTS ---

    @Test
    void findByStudentCode_ShouldReturnStudent_WhenStudentCodeExists() {
        Optional<Student> result = studentRepository.findById("ST001");

        assertTrue(result.isPresent());
        Student student = result.get();
        assertEquals("ST001", student.getStudentCode());
        assertEquals("Juan Pérez", student.getName());
        assertEquals(AcademicStatus.ACTIVE, student.getAcademicStatus());
        assertEquals(UserType.STUDENT, student.getType());
    }

    @Test
    void findBySemester_ShouldReturnStudents_WhenSemesterExists() {
        List<Student> result = studentRepository.findBySemester(5);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Student::getStudentCode)
                .containsExactlyInAnyOrder("ST001", "ST003");
    }

    @Test
    void save_ShouldPersistStudentWithAllRelations() {
        Student newStudent = new Student("ST005", "Pedro Martínez", "pedro.martinez@university.edu", "55667788");
        newStudent.setTrafficLight(trafficLight1);
        newStudent.setSchedules(Arrays.asList(schedule1, schedule2));
        newStudent.setAcademicProgram(academicProgram1);
        newStudent.setAcademicStatus(AcademicStatus.ACTIVE);
        newStudent.setDeanery(deanery1);
        newStudent.setSemester(4);

        Student savedStudent = studentRepository.save(newStudent);
        assertNotNull(savedStudent);
        assertEquals(AcademicStatus.ACTIVE, savedStudent.getAcademicStatus());
    }

    @Test
    void shouldHandleStudentWithoutAllRelations() {
        Student minimalStudent = new Student("ST006", "Minimal Student", "minimal@university.edu", "99999999");
        minimalStudent.setSemester(1);
        minimalStudent.setAcademicStatus(AcademicStatus.INACTIVE);

        Student savedStudent = studentRepository.save(minimalStudent);
        assertEquals(AcademicStatus.INACTIVE, savedStudent.getAcademicStatus());
        assertNull(savedStudent.getTrafficLight());
    }

    @Test
    void shouldHandleDBRefRelationsCorrectly() {
        Optional<Student> result = studentRepository.findById("ST001");
        assertTrue(result.isPresent());
        Student student = result.get();

        assertNotNull(student.getTrafficLight());
        assertEquals("traffic1", student.getTrafficLight().getId());
        assertNotNull(student.getAcademicProgram());
        assertEquals("program1", student.getAcademicProgram().getId());
    }
}
