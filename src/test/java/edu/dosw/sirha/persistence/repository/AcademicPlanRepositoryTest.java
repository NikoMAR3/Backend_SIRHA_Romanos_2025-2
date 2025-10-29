package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.AcademicPlan;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.AcademicPlanRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class AcademicPlanRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private AcademicPlanRepository academicPlanRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private AcademicProgram program1, program2;
    private TrafficLight trafficLight1, trafficLight2;
    private Subject subject1, subject2;
    private AcademicPlan plan1, plan2, plan3;

    @BeforeEach
    void setUp() {
        // Limpiar las colecciones antes de cada test
        mongoTemplate.dropCollection(AcademicPlan.class);
        mongoTemplate.dropCollection(AcademicProgram.class);
        mongoTemplate.dropCollection(TrafficLight.class);
        mongoTemplate.dropCollection(Subject.class);

        // Crear y guardar programas académicos
        program1 = new AcademicProgram();
        program1.setId("program1");
        program1.setName("Ingeniería de Sistemas");
        mongoTemplate.save(program1);

        program2 = new AcademicProgram();
        program2.setId("program2");
        program2.setName("Ingeniería Civil");
        mongoTemplate.save(program2);

        // Crear y guardar semáforos
        trafficLight1 = new TrafficLight();
        trafficLight1.setId("trafficLight1");
        mongoTemplate.save(trafficLight1);

        trafficLight2 = new TrafficLight();
        trafficLight2.setId("trafficLight2");
        mongoTemplate.save(trafficLight2);

        // Crear y guardar materias
        subject1 = new Subject();
        subject1.setId("subject1");
        subject1.setName("Matemáticas");
        mongoTemplate.save(subject1);

        subject2 = new Subject();
        subject2.setId("subject2");
        subject2.setName("Física");
        mongoTemplate.save(subject2);

        // Crear planes académicos
        plan1 = new AcademicPlan();
        plan1.setName("Plan 2020");
        plan1.setProgram(program1);
        plan1.setTrafficLight(trafficLight1);
        plan1.setSubjects(Arrays.asList(subject1, subject2));

        plan2 = new AcademicPlan();
        plan2.setName("Plan 2021");
        plan2.setProgram(program1);
        plan2.setTrafficLight(trafficLight2);
        plan2.setSubjects(Arrays.asList(subject1));

        plan3 = new AcademicPlan();
        plan3.setName("Plan 2019");
        plan3.setProgram(program2);
        plan3.setTrafficLight(trafficLight1);
        plan3.setSubjects(Arrays.asList(subject2));

        // Guardar planes académicos
        academicPlanRepository.saveAll(Arrays.asList(plan1, plan2, plan3));
    }

    @Test
    void findByName_ShouldReturnAcademicPlan_WhenNameExists() {
        // When
        Optional<AcademicPlan> result = academicPlanRepository.findByName("Plan 2020");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Plan 2020", result.get().getName());
        assertEquals(program1.getId(), result.get().getProgram().getId());
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<AcademicPlan> result = academicPlanRepository.findByName("Plan Inexistente");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findPlanById_ShouldReturnAcademicPlan_WhenIdExists() {
        // Given
        String planId = plan1.getId();

        // When
        Optional<AcademicPlan> result = academicPlanRepository.findPlanById(planId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(planId, result.get().getId());
        assertEquals("Plan 2020", result.get().getName());
    }

    @Test
    void findPlanById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<AcademicPlan> result = academicPlanRepository.findPlanById("id_inexistente");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByProgram_ShouldReturnAcademicPlans_WhenProgramExists() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findByProgram(program1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicPlan::getName)
                .containsExactlyInAnyOrder("Plan 2020", "Plan 2021");
    }

    @Test
    void findByProgram_ShouldReturnEmptyList_WhenNoPlansForProgram() {
        // Given
        AcademicProgram emptyProgram = new AcademicProgram();
        emptyProgram.setId("emptyProgram");
        emptyProgram.setName("Programa Vacío");
        mongoTemplate.save(emptyProgram);

        // When
        List<AcademicPlan> result = academicPlanRepository.findByProgram(emptyProgram);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByProgramId_ShouldReturnAcademicPlans_WhenProgramIdExists() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findByProgramId("program1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicPlan::getName)
                .containsExactlyInAnyOrder("Plan 2020", "Plan 2021");
    }

    @Test
    void findByProgramId_ShouldReturnEmptyList_WhenProgramIdDoesNotExist() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findByProgramId("program_inexistente");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByTrafficLightId_ShouldReturnAcademicPlans_WhenTrafficLightIdExists() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findByTrafficLightId("trafficLight1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicPlan::getName)
                .containsExactlyInAnyOrder("Plan 2020", "Plan 2019");
    }

    @Test
    void findByTrafficLightId_ShouldReturnEmptyList_WhenTrafficLightIdDoesNotExist() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findByTrafficLightId("traffic_inexistente");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findBySubjectId_ShouldReturnAcademicPlans_WhenSubjectIdExists() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findBySubjectId("subject1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicPlan::getName)
                .containsExactlyInAnyOrder("Plan 2020", "Plan 2021");
    }

    @Test
    void findBySubjectId_ShouldReturnEmptyList_WhenSubjectIdDoesNotExist() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findBySubjectId("subject_inexistente");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllAcademicPlans() {
        // When
        List<AcademicPlan> result = academicPlanRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(AcademicPlan::getName)
                .containsExactlyInAnyOrder("Plan 2020", "Plan 2021", "Plan 2019");
    }

    @Test
    void save_ShouldPersistAcademicPlanWithAllRelations() {
        // Given
        AcademicPlan newPlan = new AcademicPlan();
        newPlan.setName("Plan 2022");
        newPlan.setProgram(program2);
        newPlan.setTrafficLight(trafficLight2);
        newPlan.setSubjects(Arrays.asList(subject1, subject2));

        // When
        AcademicPlan savedPlan = academicPlanRepository.save(newPlan);

        // Then
        assertNotNull(savedPlan.getId());
        assertEquals("Plan 2022", savedPlan.getName());
        assertEquals(program2.getId(), savedPlan.getProgram().getId());
        assertEquals(trafficLight2.getId(), savedPlan.getTrafficLight().getId());
        assertEquals(2, savedPlan.getSubjects().size());

        // Verify it can be retrieved
        Optional<AcademicPlan> retrievedPlan = academicPlanRepository.findById(savedPlan.getId());
        assertTrue(retrievedPlan.isPresent());
    }

    @Test
    void delete_ShouldRemoveAcademicPlan() {
        // Given
        String planId = plan1.getId();

        // When
        academicPlanRepository.deleteById(planId);

        // Then
        Optional<AcademicPlan> result = academicPlanRepository.findById(planId);
        assertFalse(result.isPresent());
    }
}