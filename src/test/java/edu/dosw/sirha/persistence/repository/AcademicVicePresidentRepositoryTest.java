package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.AcademicVicePresident;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.persistence.repository.AcademicVicePresidentRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class AcademicVicePresidentRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private AcademicVicePresidentRepository academicVicePresidentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private AcademicVicePresident avp1, avp2, avp3;
    private Petition petition1, petition2, petition3;

    @BeforeEach
    void setUp() {
        // Limpiar las colecciones antes de cada test
        mongoTemplate.dropCollection(AcademicVicePresident.class);
        mongoTemplate.dropCollection(Petition.class);

        // Crear y guardar peticiones
        petition1 = new Petition();
        petition1.setPetitionId("petition1");
        petition1.setType(PetitionType.ADD_SUBJECT);
        petition1.setPriority(PetitionPriority.HIGH);
        petition1.setState(PetitionState.PENDING);
        petition1.setStudentId("student1");
        petition1.setJustification("Justificación 1");
        petition1.setCreationDate(LocalDateTime.now());
        mongoTemplate.save(petition1);

        petition2 = new Petition();
        petition2.setPetitionId("petition2");
        petition2.setType(PetitionType.CHANGE_GROUP);
        petition2.setPriority(PetitionPriority.MEDIUM);
        petition2.setState(PetitionState.PENDING);
        petition2.setStudentId("student2");
        petition2.setJustification("Justificación 2");
        petition2.setCreationDate(LocalDateTime.now());
        mongoTemplate.save(petition2);

        petition3 = new Petition();
        petition3.setPetitionId("petition3");
        petition3.setType(PetitionType.REMOVE_SUBJECT);
        petition3.setPriority(PetitionPriority.LOW);
        petition3.setState(PetitionState.APPROVED);
        petition3.setStudentId("student3");
        petition3.setJustification("Justificación 3");
        petition3.setCreationDate(LocalDateTime.now());
        mongoTemplate.save(petition3);

        // Crear vicerrectores académicos
        avp1 = new AcademicVicePresident();
        avp1.setId("avp1");
        avp1.setName("Dr. Carlos Rodríguez");
        avp1.setMail("carlos.rodriguez@university.edu");
        avp1.setDocument("11111111");
        avp1.setPetitionIds(Arrays.asList("petition1", "petition2"));

        avp2 = new AcademicVicePresident();
        avp2.setId("avp2");
        avp2.setName("Dra. Ana López");
        avp2.setMail("ana.lopez@university.edu");
        avp2.setDocument("22222222");
        avp2.setPetitionIds(Arrays.asList("petition1", "petition3"));

        avp3 = new AcademicVicePresident();
        avp3.setId("avp3");
        avp3.setName("Dr. Pedro Martínez");
        avp3.setMail("pedro.martinez@university.edu");
        avp3.setDocument("33333333");
        avp3.setPetitionIds(Arrays.asList("petition2"));

        // Guardar vicerrectores académicos
        academicVicePresidentRepository.saveAll(Arrays.asList(avp1, avp2, avp3));
    }

    @Test
    void findById_ShouldReturnAcademicVicePresident_WhenIdExists() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findById("avp1");

        // Then
        assertTrue(result.isPresent());
        AcademicVicePresident foundAvp = result.get();
        assertEquals("avp1", foundAvp.getId());
        assertEquals("Dr. Carlos Rodríguez", foundAvp.getName());
        assertEquals("carlos.rodriguez@university.edu", foundAvp.getMail());
        assertEquals("11111111", foundAvp.getDocument());
        assertEquals(2, foundAvp.getPetitionIds().size());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findById("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByName_ShouldReturnAcademicVicePresident_WhenNameExists() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findByName("Dr. Carlos Rodríguez");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Dr. Carlos Rodríguez", result.get().getName());
        assertEquals("avp1", result.get().getId());
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findByName("Nombre Inexistente");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMail_ShouldReturnAcademicVicePresident_WhenMailExists() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findByMail("carlos.rodriguez@university.edu");

        // Then
        assertTrue(result.isPresent());
        assertEquals("carlos.rodriguez@university.edu", result.get().getMail());
        assertEquals("Dr. Carlos Rodríguez", result.get().getName());
    }

    @Test
    void findByMail_ShouldReturnEmpty_WhenMailDoesNotExist() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findByMail("nonexistent@university.edu");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByDocument_ShouldReturnAcademicVicePresident_WhenDocumentExists() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findByDocument("11111111");

        // Then
        assertTrue(result.isPresent());
        assertEquals("11111111", result.get().getDocument());
        assertEquals("Dr. Carlos Rodríguez", result.get().getName());
    }

    @Test
    void findByDocument_ShouldReturnEmpty_WhenDocumentDoesNotExist() {
        // When
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findByDocument("00000000");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByPetitionId_ShouldReturnAcademicVicePresidents_WhenPetitionIdExists() {
        // When
        List<AcademicVicePresident> result = academicVicePresidentRepository.findByPetitionId("petition1");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AcademicVicePresident::getName)
                .containsExactlyInAnyOrder("Dr. Carlos Rodríguez", "Dra. Ana López");
    }

    @Test
    void findByPetitionId_ShouldReturnEmptyList_WhenPetitionIdDoesNotExist() {
        // When
        List<AcademicVicePresident> result = academicVicePresidentRepository.findByPetitionId("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPetitionId_ShouldHandleMultiplePetitionsCorrectly() {
        // When
        List<AcademicVicePresident> result1 = academicVicePresidentRepository.findByPetitionId("petition1");
        List<AcademicVicePresident> result2 = academicVicePresidentRepository.findByPetitionId("petition2");
        List<AcademicVicePresident> result3 = academicVicePresidentRepository.findByPetitionId("petition3");

        // Then
        assertThat(result1).hasSize(2); // avp1 y avp2
        assertThat(result2).hasSize(2); // avp1 y avp3
        assertThat(result3).hasSize(1); // avp2
    }

    @Test
    void findAll_ShouldReturnAllAcademicVicePresidents() {
        // When
        List<AcademicVicePresident> result = academicVicePresidentRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(AcademicVicePresident::getName)
                .containsExactlyInAnyOrder(
                        "Dr. Carlos Rodríguez",
                        "Dra. Ana López",
                        "Dr. Pedro Martínez"
                );
    }

    @Test
    void save_ShouldPersistAcademicVicePresidentWithAllFields() {
        // Given
        AcademicVicePresident newAvp = new AcademicVicePresident();
        newAvp.setName("Dra. Laura Sánchez");
        newAvp.setMail("laura.sanchez@university.edu");
        newAvp.setDocument("44444444");
        newAvp.setPetitionIds(Arrays.asList("petition1", "petition3"));

        // When
        AcademicVicePresident savedAvp = academicVicePresidentRepository.save(newAvp);

        // Then
        assertNotNull(savedAvp.getId());
        assertEquals("Dra. Laura Sánchez", savedAvp.getName());
        assertEquals("laura.sanchez@university.edu", savedAvp.getMail());
        assertEquals("44444444", savedAvp.getDocument());
        assertEquals(2, savedAvp.getPetitionIds().size());

        // Verify it can be retrieved
        Optional<AcademicVicePresident> retrievedAvp = academicVicePresidentRepository.findById(savedAvp.getId());
        assertTrue(retrievedAvp.isPresent());
    }

    @Test
    void update_ShouldModifyExistingAcademicVicePresident() {
        // Given
        AcademicVicePresident avpToUpdate = avp1;
        avpToUpdate.setName("Dr. Carlos Rodríguez Actualizado");
        avpToUpdate.setMail("carlos.actualizado@university.edu");
        avpToUpdate.setPetitionIds(Arrays.asList("petition1")); // Remove one petition

        // When
        AcademicVicePresident updatedAvp = academicVicePresidentRepository.save(avpToUpdate);

        // Then
        assertEquals(avpToUpdate.getId(), updatedAvp.getId());
        assertEquals("Dr. Carlos Rodríguez Actualizado", updatedAvp.getName());
        assertEquals("carlos.actualizado@university.edu", updatedAvp.getMail());
        assertEquals(1, updatedAvp.getPetitionIds().size());
    }

    @Test
    void delete_ShouldRemoveAcademicVicePresident() {
        // Given
        String avpId = avp1.getId();

        // When
        academicVicePresidentRepository.deleteById(avpId);

        // Then
        Optional<AcademicVicePresident> result = academicVicePresidentRepository.findById(avpId);
        assertFalse(result.isPresent());
    }

    @Test
    void existsById_ShouldReturnTrueForExistingAcademicVicePresident() {
        // When
        boolean exists = academicVicePresidentRepository.existsById("avp1");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingAcademicVicePresident() {
        // When
        boolean exists = academicVicePresidentRepository.existsById("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void count_ShouldReturnCorrectNumberOfAcademicVicePresidents() {
        // When
        long count = academicVicePresidentRepository.count();

        // Then
        assertEquals(3, count);
    }

    @Test
    void shouldHandleCaseSensitiveQueries() {
        // When - MongoDB queries are case sensitive by default
        Optional<AcademicVicePresident> result1 = academicVicePresidentRepository.findByName("dr. carlos rodríguez");
        Optional<AcademicVicePresident> result2 = academicVicePresidentRepository.findByMail("CARLOS.RODRIGUEZ@UNIVERSITY.EDU");

        // Then
        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
    }

    @Test
    void shouldHandleEmptyPetitionIdsList() {
        // Given
        AcademicVicePresident avpWithNoPetitions = new AcademicVicePresident();
        avpWithNoPetitions.setName("Dr. Sin Peticiones");
        avpWithNoPetitions.setMail("sin.peticiones@university.edu");
        avpWithNoPetitions.setDocument("55555555");
        avpWithNoPetitions.setPetitionIds(Arrays.asList()); // Empty list

        // When
        AcademicVicePresident savedAvp = academicVicePresidentRepository.save(avpWithNoPetitions);

        // Then
        assertNotNull(savedAvp.getId());
        assertTrue(savedAvp.getPetitionIds().isEmpty());

        // Should not be found by any petition ID
        List<AcademicVicePresident> result = academicVicePresidentRepository.findByPetitionId("petition1");
        assertThat(result).noneMatch(avp -> avp.getId().equals(savedAvp.getId()));
    }

    @Test
    void shouldHandleNullFieldsInQueries() {
        // When & Then - These should not throw exceptions but return empty results
        Optional<AcademicVicePresident> result1 = academicVicePresidentRepository.findByName(null);
        Optional<AcademicVicePresident> result2 = academicVicePresidentRepository.findByMail(null);
        Optional<AcademicVicePresident> result3 = academicVicePresidentRepository.findByDocument(null);

        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
        assertFalse(result3.isPresent());
    }

    @Test
    void shouldHandleDuplicateDocumentsPrevention() {
        // Given - Try to create a new AVP with existing document
        AcademicVicePresident duplicateAvp = new AcademicVicePresident();
        duplicateAvp.setName("Dr. Duplicado");
        duplicateAvp.setMail("duplicado@university.edu");
        duplicateAvp.setDocument("11111111"); // Same as avp1

        // When
        AcademicVicePresident savedDuplicate = academicVicePresidentRepository.save(duplicateAvp);

        // Then - MongoDB allows duplicates unless we have unique index
        assertNotNull(savedDuplicate);
        assertEquals("11111111", savedDuplicate.getDocument());

        // Both should exist in database
        List<AcademicVicePresident> allAvps = academicVicePresidentRepository.findAll();
        long countWithDocument = allAvps.stream()
                .filter(avp -> "11111111".equals(avp.getDocument()))
                .count();
        assertEquals(2, countWithDocument);
    }

    @Test
    void findByPetitionId_ShouldWorkWithComplexPetitionScenarios() {
        // Given - Create a scenario where multiple AVPs share multiple petitions
        AcademicVicePresident avp4 = new AcademicVicePresident();
        avp4.setName("Dr. Complejo");
        avp4.setMail("complejo@university.edu");
        avp4.setDocument("66666666");
        avp4.setPetitionIds(Arrays.asList("petition1", "petition2", "petition3"));
        academicVicePresidentRepository.save(avp4);

        // When
        List<AcademicVicePresident> result1 = academicVicePresidentRepository.findByPetitionId("petition1");
        List<AcademicVicePresident> result2 = academicVicePresidentRepository.findByPetitionId("petition2");
        List<AcademicVicePresident> result3 = academicVicePresidentRepository.findByPetitionId("petition3");

        // Then
        assertThat(result1).hasSize(3); // avp1, avp2, avp4
        assertThat(result2).hasSize(3); // avp1, avp3, avp4
        assertThat(result3).hasSize(2); // avp2, avp4
    }
}