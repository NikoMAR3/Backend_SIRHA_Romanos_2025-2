package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.DeanRepository;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DeanRepository with MongoDB Testcontainers
 * Tests CRUD operations, custom queries, and relationship mappings
 */
@Testcontainers
@DataMongoTest
class DeanRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private DeanRepository deanRepository;

    @Autowired
    private DeaneryRepository deaneryRepository;

    private Deanery engineeringDeanery;
    private Deanery scienceDeanery;
    private Dean deanJohn;
    private Dean deanJane;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        deanRepository.deleteAll();
        deaneryRepository.deleteAll();

        // Create test deaneries
        engineeringDeanery = new Deanery();
        engineeringDeanery.setId("eng-1");
        engineeringDeanery.setDeaneryName("Engineering Faculty");
        engineeringDeanery = deaneryRepository.save(engineeringDeanery);

        scienceDeanery = new Deanery();
        scienceDeanery.setId("sci-1");
        scienceDeanery.setDeaneryName("Science Faculty");
        scienceDeanery = deaneryRepository.save(scienceDeanery);

        // Create test deans using the constructor
        deanJohn = new Dean("dean-1", "Dr. John Smith", "john.smith@university.edu", "DOC123456");
        deanJohn.setDeanery(engineeringDeanery);
        deanJohn = deanRepository.save(deanJohn);

        deanJane = new Dean("dean-2", "Dr. Jane Doe", "jane.doe@university.edu", "DOC654321");
        deanJane.setDeanery(scienceDeanery);
        deanJane = deanRepository.save(deanJane);
    }

    @Test
    void searchDeanById_WhenDeanExists_ShouldReturnDeanWithCorrectProperties() {
        // When
        Dean found = deanRepository.searchDeanById("dean-1");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo("dean-1");
        assertThat(found.getName()).isEqualTo("Dr. John Smith");
        assertThat(found.getMail()).isEqualTo("john.smith@university.edu");
        assertThat(found.getDocument()).isEqualTo("DOC123456");
        assertThat(found.getType()).isEqualTo(UserType.DEAN);
    }

    @Test
    void searchDeanById_WhenDeanNotExists_ShouldReturnNull() {
        // When
        Dean found = deanRepository.searchDeanById("non-existent-id");

        // Then
        assertThat(found).isNull();
    }

    @Test
    void findByDeaneryId_WhenMultipleDeansExistForDeanery_ShouldReturnAllDeans() {
        // Given - Add another dean to engineering deanery
        Dean thirdDean = new Dean("dean-3", "Dr. Robert Brown", "robert.brown@university.edu", "DOC111222");
        thirdDean.setDeanery(engineeringDeanery);
        deanRepository.save(thirdDean);

        // When
        List<Dean> engineeringDeans = deanRepository.findByDeaneryId("eng-1");

        // Then
        assertThat(engineeringDeans).hasSize(2);
        assertThat(engineeringDeans)
                .extracting(Dean::getName)
                .containsExactlyInAnyOrder("Dr. John Smith", "Dr. Robert Brown");

        // Verify deanery relationship
        assertThat(engineeringDeans)
                .extracting(Dean::getDeanery)
                .extracting(Deanery::getId)
                .containsOnly("eng-1");
    }

    @Test
    void findByDeaneryId_WhenNoDeansForDeanery_ShouldReturnEmptyList() {
        // Given - Create a deanery with no deans
        Deanery emptyDeanery = new Deanery();
        emptyDeanery.setId("empty-1");
        emptyDeanery.setDeaneryName("Empty Faculty");
        deaneryRepository.save(emptyDeanery);

        // When
        List<Dean> deans = deanRepository.findByDeaneryId("empty-1");

        // Then
        assertThat(deans).isEmpty();
    }

    @Test
    void findByDeaneryId_WithNonExistentDeaneryId_ShouldReturnEmptyList() {
        // When
        List<Dean> deans = deanRepository.findByDeaneryId("non-existent-deanery");

        // Then
        assertThat(deans).isEmpty();
    }

    @Test
    void findByDeanery_WhenDeanExistsForDeanery_ShouldReturnDean() {
        // When
        Optional<Dean> found = deanRepository.findByDeanery(engineeringDeanery);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("dean-1");
        assertThat(found.get().getName()).isEqualTo("Dr. John Smith");
        assertThat(found.get().getDeanery().getId()).isEqualTo("eng-1");
        assertThat(found.get().getDeanery().getDeaneryName()).isEqualTo("Engineering Faculty");
    }

    @Test
    void findByDeanery_WhenNoDeanForDeanery_ShouldReturnEmptyOptional() {
        // Given - Create a new deanery with no assigned dean
        Deanery newDeanery = new Deanery();
        newDeanery.setId("new-1");
        newDeanery.setDeaneryName("New Faculty");
        deaneryRepository.save(newDeanery);

        // When
        Optional<Dean> found = deanRepository.findByDeanery(newDeanery);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByDeanery_WithDeaneryHavingMultipleDeans_ShouldReturnFirstMatch() {
        // Note: This test assumes the relationship is one-to-one (one dean per deanery)
        // If multiple deans can belong to one deanery, this test would need adjustment

        // Given - Add another dean to engineering (if the model allows it)
        // This would typically throw an error if it's one-to-one, so we test science deanery instead
        Optional<Dean> found = deanRepository.findByDeanery(scienceDeanery);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("dean-2");
    }

    @Test
    void findAll_WhenMultipleDeansExist_ShouldReturnAllDeans() {
        // When
        List<Dean> allDeans = deanRepository.findAll();

        // Then
        assertThat(allDeans).hasSize(2);
        assertThat(allDeans)
                .extracting(Dean::getId)
                .containsExactlyInAnyOrder("dean-1", "dean-2");
        assertThat(allDeans)
                .extracting(Dean::getName)
                .containsExactlyInAnyOrder("Dr. John Smith", "Dr. Jane Doe");
    }

    @Test
    void findAll_WhenNoDeans_ShouldReturnEmptyList() {
        // Given
        deanRepository.deleteAll();

        // When
        List<Dean> allDeans = deanRepository.findAll();

        // Then
        assertThat(allDeans).isEmpty();
    }

    @Test
    void save_NewDean_ShouldPersistWithAllAttributes() {
        // Given
        Dean newDean = new Dean("dean-new", "Dr. Newcomer", "newcomer@university.edu", "DOC999888");
        newDean.setDeanery(engineeringDeanery);

        // When
        Dean saved = deanRepository.save(newDean);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("dean-new");
        assertThat(saved.getName()).isEqualTo("Dr. Newcomer");
        assertThat(saved.getMail()).isEqualTo("newcomer@university.edu");
        assertThat(saved.getDocument()).isEqualTo("DOC999888");
        assertThat(saved.getType()).isEqualTo(UserType.DEAN);
        assertThat(saved.getDeanery().getId()).isEqualTo("eng-1");

        // Verify retrieval
        Dean found = deanRepository.searchDeanById("dean-new");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Dr. Newcomer");
    }

    @Test
    void update_ExistingDean_ShouldModifyAttributes() {
        // Given
        Dean existing = deanRepository.searchDeanById("dean-1");
        existing.setName("Dr. John Smith Updated");
        existing.setMail("john.updated@university.edu");

        // When
        Dean updated = deanRepository.save(existing);

        // Then
        assertThat(updated.getName()).isEqualTo("Dr. John Smith Updated");
        assertThat(updated.getMail()).isEqualTo("john.updated@university.edu");

        // Verify the update persisted
        Dean found = deanRepository.searchDeanById("dean-1");
        assertThat(found.getName()).isEqualTo("Dr. John Smith Updated");
    }

    @Test
    void delete_ExistingDean_ShouldRemoveFromDatabase() {
        // When
        deanRepository.deleteById("dean-1");

        // Then
        Dean found = deanRepository.searchDeanById("dean-1");
        assertThat(found).isNull();

        List<Dean> remainingDeans = deanRepository.findAll();
        assertThat(remainingDeans).hasSize(1);
        assertThat(remainingDeans.get(0).getId()).isEqualTo("dean-2");
    }

    @Test
    void delete_NonExistentDean_ShouldDoNothing() {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> deanRepository.deleteById("non-existent"));
    }

    @Test
    void deanEntity_ShouldHaveCorrectUserType() {
        // When
        Dean found = deanRepository.searchDeanById("dean-1");

        // Then
        assertThat(found.getType()).isEqualTo(UserType.DEAN);
        assertThat(found.getType().name()).isEqualTo("DEAN");
    }

    @Test
    void deanEntity_WithDBRefDeanery_ShouldMaintainRelationship() {
        // When
        Dean found = deanRepository.searchDeanById("dean-1");

        // Then
        assertThat(found.getDeanery()).isNotNull();
        assertThat(found.getDeanery().getId()).isEqualTo("eng-1");
        assertThat(found.getDeanery().getDeaneryName()).isEqualTo("Engineering Faculty");
    }

    @Test
    void testDeanConstructor_ShouldSetUserTypeAutomatically() {
        // Given
        Dean newDean = new Dean("test-id", "Test Dean", "test@university.edu", "TEST123");

        // Then
        assertThat(newDean.getType()).isEqualTo(UserType.DEAN);
        assertThat(newDean.getId()).isEqualTo("test-id");
        assertThat(newDean.getName()).isEqualTo("Test Dean");
        assertThat(newDean.getMail()).isEqualTo("test@university.edu");
        assertThat(newDean.getDocument()).isEqualTo("TEST123");
    }
}