package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.AcademicPlan;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.persistence.repository.AcademicProgramRepository;
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
class AcademicProgramRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private AcademicProgramRepository academicProgramRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Deanery deanery1, deanery2;
    private AcademicPlan plan1, plan2, plan3;
    private AcademicProgram program1, program2, program3;
    private Dean dean1, dean2;

    @BeforeEach
    void setUp() {
        // Limpiar las colecciones antes de cada test
        mongoTemplate.dropCollection(AcademicProgram.class);
        mongoTemplate.dropCollection(Deanery.class);
        mongoTemplate.dropCollection(AcademicPlan.class);
        mongoTemplate.dropCollection(Dean.class);

        // Crear y guardar decanos
        dean1 = new Dean();
        dean1.setId("dean1");
        dean1.setName("Dr. Juan Pérez");
        dean1.setMail("juan.perez@university.edu");
        dean1.setDocument("12345678");
        mongoTemplate.save(dean1);

        dean2 = new Dean();
        dean2.setId("dean2");
        dean2.setName("Dra. María García");
        dean2.setMail("maria.garcia@university.edu");
        dean2.setDocument("87654321");
        mongoTemplate.save(dean2);

        // Crear y guardar decanatos
        deanery1 = new Deanery();
        deanery1.setId("deanery1");
        deanery1.setDeaneryName("Facultad de Ingeniería");
        deanery1.setDean(dean1);
        mongoTemplate.save(deanery1);

        deanery2 = new Deanery();
        deanery2.setId("deanery2");
        deanery2.setDeaneryName("Facultad de Ciencias");
        deanery2.setDean(dean2);
        mongoTemplate.save(deanery2);

        // Crear y guardar planes académicos
        plan1 = new AcademicPlan();
        plan1.setId("plan1");
        plan1.setName("Plan 2020");
        mongoTemplate.save(plan1);

        plan2 = new AcademicPlan();
        plan2.setId("plan2");
        plan2.setName("Plan 2021");
        mongoTemplate.save(plan2);

        plan3 = new AcademicPlan();
        plan3.setId("plan3");
        plan3.setName("Plan 2019");
        mongoTemplate.save(plan3);

        // Crear programas académicos
        program1 = new AcademicProgram();
        program1.setName("Ingeniería de Sistemas");
        program1.setDeanery(deanery1);
        program1.setPlans(Arrays.asList(plan1, plan2));

        program2 = new AcademicProgram();
        program2.setName("Ingeniería Civil");
        program2.setDeanery(deanery1);
        program2.setPlans(Arrays.asList(plan3));

        program3 = new AcademicProgram();
        program3.setName("Matemáticas");
        program3.setDeanery(deanery2);
        program3.setPlans(Arrays.asList(plan1, plan3));

        // Guardar programas académicos
        academicProgramRepository.saveAll(Arrays.asList(program1, program2, program3));
    }

    @Test
    void findByName_ShouldReturnAcademicProgram_WhenNameExists() {
        // When
        Optional<AcademicProgram> result = academicProgramRepository.findByName("Ingeniería de Sistemas");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Ingeniería de Sistemas", result.get().getName());
        assertEquals(deanery1.getId(), result.get().getDeanery().getId());
        assertEquals("Facultad de Ingeniería", result.get().getDeanery().getDeaneryName());
        assertEquals(2, result.get().getPlans().size());
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<AcademicProgram> result = academicProgramRepository.findByName("Programa Inexistente");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findProgramById_ShouldReturnAcademicProgram_WhenIdExists() {
        // Given
        String programId = program1.getId();

        // When
        Optional<AcademicProgram> result = academicProgramRepository.findProgramById(programId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(programId, result.get().getId());
        assertEquals("Ingeniería de Sistemas", result.get().getName());
        assertEquals(deanery1.getId(), result.get().getDeanery().getId());
    }

    @Test
    void findProgramById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<AcademicProgram> result = academicProgramRepository.findProgramById("id_inexistente");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByDeanery_ShouldReturnAcademicPrograms_WhenDeaneryExists() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeanery(deanery1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicProgram::getName)
                .containsExactlyInAnyOrder("Ingeniería de Sistemas", "Ingeniería Civil");

        // Verify deanery information is properly loaded
        assertThat(result).allMatch(program ->
                program.getDeanery().getId().equals(deanery1.getId()) &&
                        program.getDeanery().getDeaneryName().equals("Facultad de Ingeniería")
        );
    }

    @Test
    void findByDeanery_ShouldReturnEmptyList_WhenNoProgramsForDeanery() {
        // Given
        Deanery emptyDeanery = new Deanery();
        emptyDeanery.setId("emptyDeanery");
        emptyDeanery.setDeaneryName("Facultad Vacía");
        Dean emptyDean = new Dean();
        emptyDean.setId("emptyDean");
        emptyDean.setName("Decano Vacío");
        emptyDeanery.setDean(emptyDean);
        mongoTemplate.save(emptyDean);
        mongoTemplate.save(emptyDeanery);

        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeanery(emptyDeanery);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByDeaneryId_ShouldReturnAcademicPrograms_WhenDeaneryIdExists() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeaneryId("deanery1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicProgram::getName)
                .containsExactlyInAnyOrder("Ingeniería de Sistemas", "Ingeniería Civil");

        // Verify all programs belong to the correct deanery
        assertThat(result).allMatch(program ->
                program.getDeanery().getId().equals("deanery1")
        );
    }

    @Test
    void findByDeaneryId_ShouldReturnEmptyList_WhenDeaneryIdDoesNotExist() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeaneryId("deanery_inexistente");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPlanId_ShouldReturnAcademicPrograms_WhenPlanIdExists() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByPlanId("plan1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicProgram::getName)
                .containsExactlyInAnyOrder("Ingeniería de Sistemas", "Matemáticas");
    }

    @Test
    void findByPlanId_ShouldReturnEmptyList_WhenPlanIdDoesNotExist() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByPlanId("plan_inexistente");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllAcademicPrograms() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(AcademicProgram::getName)
                .containsExactlyInAnyOrder("Ingeniería de Sistemas", "Ingeniería Civil", "Matemáticas");

        // Verify all programs have deanery information
        assertThat(result).allMatch(program -> program.getDeanery() != null);
        assertThat(result).allMatch(program -> program.getDeanery().getDeaneryName() != null);
    }

    @Test
    void save_ShouldPersistAcademicProgramWithAllRelations() {
        // Given
        AcademicProgram newProgram = new AcademicProgram();
        newProgram.setName("Ingeniería Eléctrica");
        newProgram.setDeanery(deanery1);
        newProgram.setPlans(Arrays.asList(plan2));

        // When
        AcademicProgram savedProgram = academicProgramRepository.save(newProgram);

        // Then
        assertNotNull(savedProgram.getId());
        assertEquals("Ingeniería Eléctrica", savedProgram.getName());
        assertEquals(deanery1.getId(), savedProgram.getDeanery().getId());
        assertEquals("Facultad de Ingeniería", savedProgram.getDeanery().getDeaneryName());
        assertEquals(1, savedProgram.getPlans().size());

        // Verify it can be retrieved with all relations
        Optional<AcademicProgram> retrievedProgram = academicProgramRepository.findById(savedProgram.getId());
        assertTrue(retrievedProgram.isPresent());
        assertEquals("Ingeniería Eléctrica", retrievedProgram.get().getName());
        assertEquals(deanery1.getId(), retrievedProgram.get().getDeanery().getId());
    }

    @Test
    void delete_ShouldRemoveAcademicProgram() {
        // Given
        String programId = program1.getId();

        // When
        academicProgramRepository.deleteById(programId);

        // Then
        Optional<AcademicProgram> result = academicProgramRepository.findById(programId);
        assertFalse(result.isPresent());
    }

    @Test
    void update_ShouldModifyExistingAcademicProgram() {
        // Given
        AcademicProgram programToUpdate = program1;
        programToUpdate.setName("Ingeniería de Sistemas Actualizado");
        programToUpdate.setDeanery(deanery2); // Change deanery
        programToUpdate.setPlans(Arrays.asList(plan1)); // Remove one plan

        // When
        AcademicProgram updatedProgram = academicProgramRepository.save(programToUpdate);

        // Then
        assertEquals(programToUpdate.getId(), updatedProgram.getId());
        assertEquals("Ingeniería de Sistemas Actualizado", updatedProgram.getName());
        assertEquals(deanery2.getId(), updatedProgram.getDeanery().getId());
        assertEquals(1, updatedProgram.getPlans().size());
    }

    @Test
    void findByPlanId_ShouldHandleComplexRelationsCorrectly() {
        // Given - plan1 está en program1 y program3
        // When
        List<AcademicProgram> programsWithPlan1 = academicProgramRepository.findByPlanId("plan1");
        List<AcademicProgram> programsWithPlan2 = academicProgramRepository.findByPlanId("plan2");
        List<AcademicProgram> programsWithPlan3 = academicProgramRepository.findByPlanId("plan3");

        // Then
        assertThat(programsWithPlan1).hasSize(2);
        assertThat(programsWithPlan2).hasSize(1);
        assertThat(programsWithPlan3).hasSize(2);

        assertThat(programsWithPlan1).extracting(AcademicProgram::getName)
                .containsExactlyInAnyOrder("Ingeniería de Sistemas", "Matemáticas");
        assertThat(programsWithPlan2).extracting(AcademicProgram::getName)
                .containsExactly("Ingeniería de Sistemas");
        assertThat(programsWithPlan3).extracting(AcademicProgram::getName)
                .containsExactlyInAnyOrder("Ingeniería Civil", "Matemáticas");
    }

    @Test
    void shouldHandleDeaneryWithDeanRelationship() {
        // Given - Retrieve a program and check its deanery has dean information
        Optional<AcademicProgram> programOpt = academicProgramRepository.findByName("Ingeniería de Sistemas");

        // Then
        assertTrue(programOpt.isPresent());
        AcademicProgram program = programOpt.get();
        assertNotNull(program.getDeanery());
        assertNotNull(program.getDeanery().getDean());
        assertEquals("Dr. Juan Pérez", program.getDeanery().getDean().getName());
        assertEquals("juan.perez@university.edu", program.getDeanery().getDean().getMail());
    }

    @Test
    void findByDeaneryId_ShouldReturnProgramsWithCompleteDeaneryInfo() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeaneryId("deanery1");

        // Then
        assertThat(result).hasSize(2);

        // Verify deanery information is complete
        result.forEach(program -> {
            assertNotNull(program.getDeanery());
            assertEquals("Facultad de Ingeniería", program.getDeanery().getDeaneryName());
            assertNotNull(program.getDeanery().getDean());
            assertEquals("Dr. Juan Pérez", program.getDeanery().getDean().getName());
        });
    }

    @Test
    void shouldHandleCaseSensitiveProgramNames() {
        // When
        Optional<AcademicProgram> result1 = academicProgramRepository.findByName("ingeniería de sistemas");
        Optional<AcademicProgram> result2 = academicProgramRepository.findByName("INGENIERÍA DE SISTEMAS");

        // Then - MongoDB queries are case sensitive by default
        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
    }

    @Test
    void shouldReturnEmptyListWhenFindingByNullDeanery() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeanery(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFindingByNullDeaneryId() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeaneryId(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFindingByEmptyDeaneryId() {
        // When
        List<AcademicProgram> result = academicProgramRepository.findByDeaneryId("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void count_ShouldReturnCorrectNumberOfPrograms() {
        long count = academicProgramRepository.count();
        assertEquals(3, count);
    }

    @Test
    void existsById_ShouldReturnTrueForExistingProgram() {
        String existingId = program1.getId();
        boolean exists = academicProgramRepository.existsById(existingId);
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingProgram() {
        boolean exists = academicProgramRepository.existsById("non-existing-id");
        assertFalse(exists);
    }
}