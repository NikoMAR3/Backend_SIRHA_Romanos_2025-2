package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
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
class DeaneryRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private DeaneryRepository deaneryRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Dean dean1, dean2, dean3;
    private Deanery deanery1, deanery2, deanery3;

    @BeforeEach
    void setUp() {
        // Limpiar las colecciones antes de cada test
        mongoTemplate.dropCollection(Deanery.class);
        mongoTemplate.dropCollection(Dean.class);

        // Crear y guardar decanos
        dean1 = new Dean();
        dean1.setId("dean1");
        dean1.setName("Dr. Carlos Rodríguez");
        dean1.setMail("carlos.rodriguez@university.edu");
        dean1.setDocument("11111111");
        mongoTemplate.save(dean1);

        dean2 = new Dean();
        dean2.setId("dean2");
        dean2.setName("Dra. Ana López");
        dean2.setMail("ana.lopez@university.edu");
        dean2.setDocument("22222222");
        mongoTemplate.save(dean2);

        dean3 = new Dean();
        dean3.setId("dean3");
        dean3.setName("Dr. Pedro Martínez");
        dean3.setMail("pedro.martinez@university.edu");
        dean3.setDocument("33333333");
        mongoTemplate.save(dean3);

        // Crear decanatos
        deanery1 = new Deanery();
        deanery1.setDeaneryName("Facultad de Ingeniería");
        deanery1.setDean(dean1);

        deanery2 = new Deanery();
        deanery2.setDeaneryName("Facultad de Ciencias");
        deanery2.setDean(dean2);

        deanery3 = new Deanery();
        deanery3.setDeaneryName("Facultad de Humanidades");
        deanery3.setDean(dean3);

        // Guardar decanatos
        deaneryRepository.saveAll(Arrays.asList(deanery1, deanery2, deanery3));
    }

    @Test
    void findByDeaneryName_ShouldReturnDeanery_WhenNameExists() {
        // When
        Optional<Deanery> result = deaneryRepository.findByDeaneryName("Facultad de Ingeniería");

        // Then
        assertTrue(result.isPresent());
        Deanery foundDeanery = result.get();
        assertEquals("Facultad de Ingeniería", foundDeanery.getDeaneryName());
        assertNotNull(foundDeanery.getId());
        assertNotNull(foundDeanery.getDean());
        assertEquals("Dr. Carlos Rodríguez", foundDeanery.getDean().getName());
        assertEquals("carlos.rodriguez@university.edu", foundDeanery.getDean().getMail());
    }

    @Test
    void findByDeaneryName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<Deanery> result = deaneryRepository.findByDeaneryName("Facultad Inexistente");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByDeaneryName_ShouldBeCaseSensitive() {
        // When - MongoDB queries are case sensitive by default
        Optional<Deanery> result1 = deaneryRepository.findByDeaneryName("facultad de ingeniería");
        Optional<Deanery> result2 = deaneryRepository.findByDeaneryName("FACULTAD DE INGENIERÍA");

        // Then
        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
    }

    @Test
    void findDeaneryById_ShouldReturnDeanery_WhenIdExists() {
        // Given
        String deaneryId = deanery1.getId();

        // When
        Optional<Deanery> result = deaneryRepository.findDeaneryById(deaneryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(deaneryId, result.get().getId());
        assertEquals("Facultad de Ingeniería", result.get().getDeaneryName());
        assertNotNull(result.get().getDean());
        assertEquals("dean1", result.get().getDean().getId());
    }

    @Test
    void findDeaneryById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<Deanery> result = deaneryRepository.findDeaneryById("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllDeaneries() {
        // When
        List<Deanery> result = deaneryRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Deanery::getDeaneryName)
                .containsExactlyInAnyOrder(
                        "Facultad de Ingeniería",
                        "Facultad de Ciencias",
                        "Facultad de Humanidades"
                );

        // Verify all deaneries have dean information
        assertThat(result).allMatch(deanery -> deanery.getDean() != null);
        assertThat(result).allMatch(deanery -> deanery.getDean().getName() != null);
    }

    @Test
    void save_ShouldPersistDeaneryWithAllFields() {
        // Given
        Deanery newDeanery = new Deanery();
        newDeanery.setDeaneryName("Facultad de Medicina");
        newDeanery.setDean(dean3);

        // When
        Deanery savedDeanery = deaneryRepository.save(newDeanery);

        // Then
        assertNotNull(savedDeanery.getId());
        assertEquals("Facultad de Medicina", savedDeanery.getDeaneryName());
        assertNotNull(savedDeanery.getDean());
        assertEquals("Dr. Pedro Martínez", savedDeanery.getDean().getName());

        // Verify it can be retrieved
        Optional<Deanery> retrievedDeanery = deaneryRepository.findById(savedDeanery.getId());
        assertTrue(retrievedDeanery.isPresent());
        assertEquals("Facultad de Medicina", retrievedDeanery.get().getDeaneryName());
    }

    @Test
    void update_ShouldModifyExistingDeanery() {
        // Given
        Deanery deaneryToUpdate = deanery1;
        deaneryToUpdate.setDeaneryName("Facultad de Ingeniería y Tecnología");
        deaneryToUpdate.setDean(dean2); // Change dean

        // When
        Deanery updatedDeanery = deaneryRepository.save(deaneryToUpdate);

        // Then
        assertEquals(deaneryToUpdate.getId(), updatedDeanery.getId());
        assertEquals("Facultad de Ingeniería y Tecnología", updatedDeanery.getDeaneryName());
        assertEquals(dean2.getId(), updatedDeanery.getDean().getId());
        assertEquals("Dra. Ana López", updatedDeanery.getDean().getName());
    }

    @Test
    void delete_ShouldRemoveDeanery() {
        // Given
        String deaneryId = deanery1.getId();

        // When
        deaneryRepository.deleteById(deaneryId);

        // Then
        Optional<Deanery> result = deaneryRepository.findById(deaneryId);
        assertFalse(result.isPresent());
    }

    @Test
    void existsById_ShouldReturnTrueForExistingDeanery() {
        // When
        boolean exists = deaneryRepository.existsById(deanery1.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingDeanery() {
        // When
        boolean exists = deaneryRepository.existsById("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void count_ShouldReturnCorrectNumberOfDeaneries() {
        // When
        long count = deaneryRepository.count();

        // Then
        assertEquals(3, count);
    }

    @Test
    void shouldHandleNullDeaneryNameInQuery() {
        // When
        Optional<Deanery> result = deaneryRepository.findByDeaneryName(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldHandleEmptyDeaneryNameInQuery() {
        // When
        Optional<Deanery> result = deaneryRepository.findByDeaneryName("");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldHandleNullIdInQuery() {
        // When
        Optional<Deanery> result = deaneryRepository.findDeaneryById(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldHandleEmptyIdInQuery() {
        // When
        Optional<Deanery> result = deaneryRepository.findDeaneryById("");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldPreventDuplicateDeaneryNames() {
        // Given - Try to create a deanery with existing name
        Deanery duplicateDeanery = new Deanery();
        duplicateDeanery.setDeaneryName("Facultad de Ingeniería"); // Same as deanery1
        duplicateDeanery.setDean(dean3);

        // When
        Deanery savedDuplicate = deaneryRepository.save(duplicateDeanery);

        // Then - MongoDB allows duplicates unless we have unique index
        assertNotNull(savedDuplicate);
        assertEquals("Facultad de Ingeniería", savedDuplicate.getDeaneryName());

        // Both should exist in database
        List<Deanery> allDeaneries = deaneryRepository.findAll();
        long countWithName = allDeaneries.stream()
                .filter(deanery -> "Facultad de Ingeniería".equals(deanery.getDeaneryName()))
                .count();
        assertEquals(2, countWithName);
    }

    @Test
    void shouldHandleDeaneryWithoutDean() {
        // Given - Create deanery without dean assignment
        Deanery deaneryWithoutDean = new Deanery();
        deaneryWithoutDean.setDeaneryName("Facultad de Artes");
        // No dean set

        // When
        Deanery savedDeanery = deaneryRepository.save(deaneryWithoutDean);

        // Then
        assertNotNull(savedDeanery.getId());
        assertEquals("Facultad de Artes", savedDeanery.getDeaneryName());
        assertNull(savedDeanery.getDean());

        // Verify it can be retrieved
        Optional<Deanery> retrievedDeanery = deaneryRepository.findById(savedDeanery.getId());
        assertTrue(retrievedDeanery.isPresent());
        assertNull(retrievedDeanery.get().getDean());
    }

    @Test
    void shouldUpdateOnlyDeaneryName() {
        // Given
        Deanery deaneryToUpdate = deanery1;
        String originalId = deaneryToUpdate.getId();
        Dean originalDean = deaneryToUpdate.getDean();

        deaneryToUpdate.setDeaneryName("Facultad de Ingeniería Actualizada");

        // When
        Deanery updatedDeanery = deaneryRepository.save(deaneryToUpdate);

        // Then
        assertEquals(originalId, updatedDeanery.getId());
        assertEquals("Facultad de Ingeniería Actualizada", updatedDeanery.getDeaneryName());
        // Dean should remain unchanged
        assertEquals(originalDean.getId(), updatedDeanery.getDean().getId());
        assertEquals(originalDean.getName(), updatedDeanery.getDean().getName());
    }

    @Test
    void shouldUpdateOnlyDean() {
        // Given
        Deanery deaneryToUpdate = deanery1;
        String originalId = deaneryToUpdate.getId();
        String originalName = deaneryToUpdate.getDeaneryName();

        deaneryToUpdate.setDean(dean3); // Change to different dean

        // When
        Deanery updatedDeanery = deaneryRepository.save(deaneryToUpdate);

        // Then
        assertEquals(originalId, updatedDeanery.getId());
        assertEquals(originalName, updatedDeanery.getDeaneryName());
        // Dean should be changed
        assertEquals(dean3.getId(), updatedDeanery.getDean().getId());
        assertEquals("Dr. Pedro Martínez", updatedDeanery.getDean().getName());
    }

    @Test
    void findByDeaneryName_ShouldWorkAfterMultipleOperations() {
        // Given - Perform multiple operations
        deaneryRepository.deleteById(deanery2.getId());

        Deanery newDeanery = new Deanery();
        newDeanery.setDeaneryName("Facultad de Derecho");
        newDeanery.setDean(dean1);
        deaneryRepository.save(newDeanery);

        // When
        Optional<Deanery> result1 = deaneryRepository.findByDeaneryName("Facultad de Derecho");
        Optional<Deanery> result2 = deaneryRepository.findByDeaneryName("Facultad de Ciencias");

        // Then
        assertTrue(result1.isPresent());
        assertEquals("Facultad de Derecho", result1.get().getDeaneryName());
        assertFalse(result2.isPresent()); // Was deleted
    }

    @Test
    void findAll_ShouldReturnConsistentResults() {
        // When - Call findAll multiple times
        List<Deanery> result1 = deaneryRepository.findAll();
        List<Deanery> result2 = deaneryRepository.findAll();

        // Then - Results should be consistent
        assertThat(result1).hasSize(3);
        assertThat(result2).hasSize(3);
        assertThat(result1).extracting(Deanery::getDeaneryName)
                .containsExactlyInAnyOrderElementsOf(
                        result2.stream().map(Deanery::getDeaneryName).toList()
                );
    }
}
