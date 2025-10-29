package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class ScheduleRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Subject subject1;
    private Subject subject2;
    private Schedule schedule1;
    private Schedule schedule2;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();

        subject1 = new Subject("sub001", "MAT01", "Matemáticas", null, 3, 1, null, null);
        subject2 = new Subject("sub002", "FIS01", "Física", null, 3, 1, null, null);

        schedule1 = new Schedule();
        schedule1.setId("sch001");
        schedule1.setStudentId("stu001");
        schedule1.setSubjects(List.of(subject1));
        schedule1.setName("Horario 1");
        schedule1.setClassroom("A101");
        schedule1.setDayOfWeek("Lunes");
        schedule1.setStartTime(LocalDateTime.of(2024, 3, 1, 8, 0));
        schedule1.setEndTime(LocalDateTime.of(2024, 3, 1, 10, 0));
        schedule1.setSemester("1");
        schedule1.setCredits(3);
        schedule1.setProgram("SIS");

        schedule2 = new Schedule();
        schedule2.setId("sch002");
        schedule2.setStudentId("stu002");
        schedule2.setSubjects(List.of(subject2));
        schedule2.setName("Horario 2");
        schedule2.setClassroom("B202");
        schedule2.setDayOfWeek("Martes");
        schedule2.setStartTime(LocalDateTime.of(2024, 3, 2, 8, 0));
        schedule2.setEndTime(LocalDateTime.of(2024, 3, 2, 10, 0));
        schedule2.setSemester("1");
        schedule2.setCredits(3);
        schedule2.setProgram("ELE");

        scheduleRepository.saveAll(List.of(schedule1, schedule2));
    }

    @Test
    void shouldFindByStudentId() {
        Optional<Schedule> result = scheduleRepository.findByStudentId("stu001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Horario 1");
    }

    @Test
    void shouldFindBySubjectId() {
        List<Schedule> result = scheduleRepository.findBySubjectId("sub001");

        assertThat(result)
                .extracting(Schedule::getStudentId)
                .containsExactly("stu001");
    }

    @Test
    void shouldFindBySubjectShortName() {
        List<Schedule> result = scheduleRepository.findBySubjectShortName("FIS01");

        assertThat(result)
                .extracting(Schedule::getStudentId)
                .containsExactly("stu002");
    }

    @Test
    void shouldFindBySubjectName() {
        List<Schedule> result = scheduleRepository.findBySubjectName("Matemáticas");

        assertThat(result)
                .extracting(Schedule::getStudentId)
                .containsExactly("stu001");
    }

    @Test
    void shouldFindBySemester() {
        List<Schedule> result = scheduleRepository.findBySemester("1");

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindByProgram() {
        List<Schedule> result = scheduleRepository.findByProgram("SIS");

        assertThat(result)
                .extracting(Schedule::getStudentId)
                .containsExactly("stu001");
    }

    @Test
    void shouldCountBySubjectId() {
        long count = scheduleRepository.countBySubjectId("sub002");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldExistByStudentId() {
        boolean exists = scheduleRepository.existsByStudentId("stu001");

        assertThat(exists).isTrue();
    }
    @Test
    void shouldDeleteByStudentId() {
        // Given
        assertThat(scheduleRepository.findByStudentId("stu001")).isPresent();

        // When
        scheduleRepository.deleteByStudentId("stu001");

        // Then
        Optional<Schedule> result = scheduleRepository.findByStudentId("stu001");
        assertThat(result).isEmpty();

        // And only the other schedule remains
        List<Schedule> remaining = scheduleRepository.findAll();
        assertThat(remaining)
                .extracting(Schedule::getStudentId)
                .containsExactly("stu002");
    }

    @Test
    void shouldDeleteBySemester() {
        // Given
        assertThat(scheduleRepository.findBySemester("1")).hasSize(2);

        // When
        scheduleRepository.deleteBySemester("1");

        // Then
        List<Schedule> result = scheduleRepository.findBySemester("1");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindByProgramOrderBySemesterDesc() {
        // Given: agregar otro schedule con semestre distinto para el mismo programa
        Schedule schedule3 = new Schedule();
        schedule3.setId("sch003");
        schedule3.setStudentId("stu003");
        schedule3.setSubjects(List.of(subject1));
        schedule3.setName("Horario 3");
        schedule3.setClassroom("C303");
        schedule3.setDayOfWeek("Miércoles");
        schedule3.setStartTime(LocalDateTime.of(2024, 3, 3, 8, 0));
        schedule3.setEndTime(LocalDateTime.of(2024, 3, 3, 10, 0));
        schedule3.setSemester("2");
        schedule3.setCredits(3);
        schedule3.setProgram("SIS");
        scheduleRepository.save(schedule3);

        // When
        List<Schedule> result = scheduleRepository.findByProgramOrderBySemesterDesc("SIS");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSemester()).isEqualTo("2");
        assertThat(result.get(1).getSemester()).isEqualTo("1");
    }
    @Test
    void shouldFindAllByOrderByStudentIdAscSemesterDesc() {
        // Given: agregar un nuevo horario con un semestre diferente
        Schedule schedule3 = new Schedule();
        schedule3.setId("sch003");
        schedule3.setStudentId("stu001"); // mismo estudiante que schedule1
        schedule3.setSubjects(List.of(subject1));
        schedule3.setName("Horario 3");
        schedule3.setClassroom("C303");
        schedule3.setDayOfWeek("Miércoles");
        schedule3.setStartTime(LocalDateTime.of(2024, 3, 3, 8, 0));
        schedule3.setEndTime(LocalDateTime.of(2024, 3, 3, 10, 0));
        schedule3.setSemester("2");
        schedule3.setCredits(3);
        schedule3.setProgram("SIS");
        scheduleRepository.save(schedule3);

        // When
        List<Schedule> result = scheduleRepository.findAllByOrderByStudentIdAscSemesterDesc();

        // Then
        assertThat(result).hasSize(3);
        // Verificar orden: primero stu001 semestre 2, luego stu001 semestre 1, luego stu002 semestre 1
        assertThat(result.get(0).getStudentId()).isEqualTo("stu001");
        assertThat(result.get(0).getSemester()).isEqualTo("2");
        assertThat(result.get(1).getStudentId()).isEqualTo("stu001");
        assertThat(result.get(1).getSemester()).isEqualTo("1");
        assertThat(result.get(2).getStudentId()).isEqualTo("stu002");
    }

    @Test
    void shouldFindByProgramAndSemester() {
        // When
        List<Schedule> result = scheduleRepository.findByProgramAndSemester("SIS", "1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo("stu001");
    }

    @Test
    void shouldFindByStudentIdAndSemester() {
        // When
        List<Schedule> result = scheduleRepository.findByStudentIdAndSemester("stu002", "1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProgram()).isEqualTo("ELE");
    }
    @Test
    void shouldCheckExistsByStudentIdAndSemester_WhenScheduleExists() {
        // When
        boolean exists = scheduleRepository.existsByStudentIdAndSemester("stu001", "1");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckExistsByStudentIdAndSemester_WhenScheduleDoesNotExist() {
        // When
        boolean exists = scheduleRepository.existsByStudentIdAndSemester("stu999", "1");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldCountBySemester() {
        // When
        long count = scheduleRepository.countBySemester("1");

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCountByProgram() {
        // When
        long countSIS = scheduleRepository.countByProgram("SIS");
        long countELE = scheduleRepository.countByProgram("ELE");
        long countNonExisting = scheduleRepository.countByProgram("BIO");

        // Then
        assertThat(countSIS).isEqualTo(1);
        assertThat(countELE).isEqualTo(1);
        assertThat(countNonExisting).isZero();
    }
}
