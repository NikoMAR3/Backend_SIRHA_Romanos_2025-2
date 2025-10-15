package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.TrafficLightStatus;
import edu.dosw.sirha.model.persistence.repository.TrafficLightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class TrafficLightRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private TrafficLightRepository trafficLightRepository;

    private Subject mathSubject;
    private Subject physicsSubject;
    private Subject chemistrySubject;
    private TrafficLight trafficLight1;
    private TrafficLight trafficLight2;
    private TrafficLight trafficLight3;

    @BeforeEach
    void setUp() {
        trafficLightRepository.deleteAll();

        // Crear subjects de ejemplo
        mathSubject = new Subject();
        mathSubject.setId("MATH001");
        mathSubject.setShortName("MATH101");
        mathSubject.setName("Mathematics");
        mathSubject.setCredits(4);
        mathSubject.setLevel(1);
        mathSubject.setProgramId("PROG1");

        physicsSubject = new Subject();
        physicsSubject.setId("PHYS001");
        physicsSubject.setShortName("PHYS201");
        physicsSubject.setName("Physics");
        physicsSubject.setCredits(6);
        physicsSubject.setLevel(2);
        physicsSubject.setProgramId("PROG1");

        chemistrySubject = new Subject();
        chemistrySubject.setId("CHEM001");
        chemistrySubject.setShortName("CHEM301");
        chemistrySubject.setName("Chemistry");
        chemistrySubject.setCredits(8);
        chemistrySubject.setLevel(3);
        chemistrySubject.setProgramId("PROG2");

        // Crear TrafficLight instances
        HashMap<Subject, Integer> failedSubjects1 = new HashMap<>();
        failedSubjects1.put(mathSubject, 2); // 2 intentos fallidos

        HashMap<Subject, Integer> approvedSubjects1 = new HashMap<>();
        approvedSubjects1.put(physicsSubject, 85); // Calificación 85

        trafficLight1 = new TrafficLight();
        trafficLight1.setId("TL001");
        trafficLight1.setStudentId("STU001");
        trafficLight1.setProgramId("PROG1");
        trafficLight1.setSubjectShortName("MATH101");
        trafficLight1.setSubjectName("Mathematics");
        trafficLight1.setFailedSubjects(failedSubjects1);
        trafficLight1.setApprovedSubjects(approvedSubjects1);
        trafficLight1.setOnGoingSubjects(Arrays.asList(chemistrySubject));
        trafficLight1.setUnseenSubjects(Arrays.asList(physicsSubject));
        trafficLight1.setSemester(3);
        trafficLight1.setStatus(TrafficLightStatus.RED);
        trafficLight1.setGrade(2.8);
        trafficLight1.setCredits(45);

        HashMap<Subject, Integer> approvedSubjects2 = new HashMap<>();
        approvedSubjects2.put(mathSubject, 90);
        approvedSubjects2.put(physicsSubject, 88);

        trafficLight2 = new TrafficLight();
        trafficLight2.setId("TL002");
        trafficLight2.setStudentId("STU002");
        trafficLight2.setProgramId("PROG1");
        trafficLight2.setSubjectShortName("PHYS201");
        trafficLight2.setSubjectName("Physics");
        trafficLight2.setFailedSubjects(new HashMap<>());
        trafficLight2.setApprovedSubjects(approvedSubjects2);
        trafficLight2.setOnGoingSubjects(Arrays.asList(chemistrySubject));
        trafficLight2.setUnseenSubjects(Arrays.asList());
        trafficLight2.setSemester(4);
        trafficLight2.setStatus(TrafficLightStatus.GREEN);
        trafficLight2.setGrade(4.2);
        trafficLight2.setCredits(60);

        HashMap<Subject, Integer> failedSubjects3 = new HashMap<>();
        failedSubjects3.put(chemistrySubject, 1);

        trafficLight3 = new TrafficLight();
        trafficLight3.setId("TL003");
        trafficLight3.setStudentId("STU001"); // Mismo estudiante, diferente programa
        trafficLight3.setProgramId("PROG2");
        trafficLight3.setSubjectShortName("CHEM301");
        trafficLight3.setSubjectName("Chemistry");
        trafficLight3.setFailedSubjects(failedSubjects3);
        trafficLight3.setApprovedSubjects(new HashMap<>());
        trafficLight3.setOnGoingSubjects(Arrays.asList(mathSubject));
        trafficLight3.setUnseenSubjects(Arrays.asList(physicsSubject));
        trafficLight3.setSemester(2);
        trafficLight3.setStatus(TrafficLightStatus.BLUE);
        trafficLight3.setGrade(3.1);
        trafficLight3.setCredits(30);

        trafficLightRepository.saveAll(Arrays.asList(trafficLight1, trafficLight2, trafficLight3));
    }

    @Test
    void findByStudentId_WhenExists_ShouldReturnTrafficLight() {
        Optional<TrafficLight> found = trafficLightRepository.findByStudentId("STU002");

        assertThat(found).isPresent();
        assertThat(found.get().getProgramId()).isEqualTo("PROG1");
        assertThat(found.get().getStatus()).isEqualTo(TrafficLightStatus.GREEN);
    }

    @Test
    void findByStudentId_WhenNotExists_ShouldReturnEmpty() {
        Optional<TrafficLight> found = trafficLightRepository.findByStudentId("STU999");

        assertThat(found).isEmpty();
    }

    @Test
    void findAllByStudentId_ShouldReturnAllForStudent() {
        List<TrafficLight> found = trafficLightRepository.findAllByStudentId("STU001");

        assertThat(found).hasSize(2);
        assertThat(found)
                .extracting(TrafficLight::getProgramId)
                .containsExactlyInAnyOrder("PROG1", "PROG2");
    }

    @Test
    void findByProgramId_ShouldReturnTrafficLightsForProgram() {
        List<TrafficLight> found = trafficLightRepository.findByProgramId("PROG1");

        assertThat(found).hasSize(2);
        assertThat(found)
                .extracting(TrafficLight::getStudentId)
                .containsExactlyInAnyOrder("STU001", "STU002");
    }

    @Test
    void findByStatus_ShouldReturnTrafficLightsWithStatus() {
        List<TrafficLight> found = trafficLightRepository.findByStatus(TrafficLightStatus.RED);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStudentId()).isEqualTo("STU001");
        assertThat(found.get(0).getGrade()).isEqualTo(2.8);
    }

    @Test
    void findByStudentIdAndProgramId_ShouldReturnSpecificTrafficLight() {
        Optional<TrafficLight> found = trafficLightRepository.findByStudentIdAndProgramId("STU001", "PROG1");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("TL001");
        assertThat(found.get().getSubjectShortName()).isEqualTo("MATH101");
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        long redCount = trafficLightRepository.countByStatus(TrafficLightStatus.RED);
        long greenCount = trafficLightRepository.countByStatus(TrafficLightStatus.GREEN);
        long blueCount = trafficLightRepository.countByStatus(TrafficLightStatus.BLUE);

        assertThat(redCount).isEqualTo(1);
        assertThat(greenCount).isEqualTo(1);
        assertThat(blueCount).isEqualTo(1);
    }

    @Test
    void countByProgramId_ShouldReturnCorrectCount() {
        long prog1Count = trafficLightRepository.countByProgramId("PROG1");
        long prog2Count = trafficLightRepository.countByProgramId("PROG2");

        assertThat(prog1Count).isEqualTo(2);
        assertThat(prog2Count).isEqualTo(1);
    }

    @Test
    void findBySemester_ShouldReturnTrafficLightsForSemester() {
        List<TrafficLight> semester3Lights = trafficLightRepository.findBySemester(3);
        List<TrafficLight> semester4Lights = trafficLightRepository.findBySemester(4);

        assertThat(semester3Lights).hasSize(1);
        assertThat(semester3Lights.get(0).getStudentId()).isEqualTo("STU001");

        assertThat(semester4Lights).hasSize(1);
        assertThat(semester4Lights.get(0).getStudentId()).isEqualTo("STU002");
    }

    @Test
    void findByGradeGreaterThan_ShouldReturnTrafficLightsWithHigherGrades() {
        List<TrafficLight> found = trafficLightRepository.findByGradeGreaterThan(3.5);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStudentId()).isEqualTo("STU002");
        assertThat(found.get(0).getGrade()).isEqualTo(4.2);
    }

    @Test
    void findByGradeLessThan_ShouldReturnTrafficLightsWithLowerGrades() {
        List<TrafficLight> found = trafficLightRepository.findByGradeLessThan(3.0);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStudentId()).isEqualTo("STU001");
        assertThat(found.get(0).getGrade()).isEqualTo(2.8);
    }

    @Test
    void saveTrafficLight_WithSubjectMaps_ShouldMaintainData() {
        TrafficLight newTrafficLight = new TrafficLight();
        newTrafficLight.setId("TL004");
        newTrafficLight.setStudentId("STU003");
        newTrafficLight.setProgramId("PROG1");
        newTrafficLight.setSubjectShortName("TEST101");
        newTrafficLight.setSubjectName("Test Subject");

        HashMap<Subject, Integer> failed = new HashMap<>();
        failed.put(mathSubject, 1);

        HashMap<Subject, Integer> approved = new HashMap<>();
        approved.put(physicsSubject, 92);

        newTrafficLight.setFailedSubjects(failed);
        newTrafficLight.setApprovedSubjects(approved);
        newTrafficLight.setOnGoingSubjects(Arrays.asList(chemistrySubject));
        newTrafficLight.setUnseenSubjects(Arrays.asList());
        newTrafficLight.setSemester(1);
        newTrafficLight.setStatus(TrafficLightStatus.WHITE);
        newTrafficLight.setGrade(3.8);
        newTrafficLight.setCredits(15);

        TrafficLight saved = trafficLightRepository.save(newTrafficLight);

        Optional<TrafficLight> retrieved = trafficLightRepository.findById("TL004");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFailedSubjects()).hasSize(1);
        assertThat(retrieved.get().getApprovedSubjects()).hasSize(1);
        assertThat(retrieved.get().getOnGoingSubjects()).hasSize(1);
        assertThat(retrieved.get().getStatus()).isEqualTo(TrafficLightStatus.WHITE);
    }

    @Test
    void deleteTrafficLight_ShouldRemoveFromDatabase() {
        trafficLightRepository.delete(trafficLight1);

        Optional<TrafficLight> found = trafficLightRepository.findById("TL001");
        assertThat(found).isEmpty();

        List<TrafficLight> all = trafficLightRepository.findAll();
        assertThat(all).hasSize(2);
    }
}
