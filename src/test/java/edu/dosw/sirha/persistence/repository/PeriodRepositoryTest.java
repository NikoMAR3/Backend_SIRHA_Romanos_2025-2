package edu.dosw.sirha.persistence.repository;


import edu.dosw.sirha.model.entities.Period;
import edu.dosw.sirha.model.persistence.repository.PeriodRepository;
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
class PeriodRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Period period1, period2, period3, period4;

    @BeforeEach
    void setUp() {
        // Limpiar la colección antes de cada test
        mongoTemplate.dropCollection(Period.class);

        // Crear períodos académicos
        period1 = new Period();
        period1.setStartDate("2024-01-15");
        period1.setEndDate("2024-05-15");
        period1.setEnabled(true);

        period2 = new Period();
        period2.setStartDate("2024-06-01");
        period2.setEndDate("2024-07-31");
        period2.setEnabled(false);

        period3 = new Period();
        period3.setStartDate("2024-08-01");
        period3.setEndDate("2024-12-15");
        period3.setEnabled(true);

        period4 = new Period();
        period4.setStartDate("2023-08-01");
        period4.setEndDate("2023-12-15");
        period4.setEnabled(true);

        // Guardar períodos académicos
        periodRepository.saveAll(Arrays.asList(period1, period2, period3, period4));
    }

    @Test
    void findEnabledPeriods_ShouldReturnOnlyEnabledPeriods() {
        // When
        List<Period> result = periodRepository.findEnabledPeriods();

        // Then
        assertThat(result).hasSize(3); // period1, period3, period4 are enabled
        assertThat(result).extracting(Period::getEnabled)
                .containsOnly(true);
        assertThat(result).extracting(Period::getStartDate)
                .containsExactlyInAnyOrder("2024-01-15", "2024-08-01", "2023-08-01");
    }

    @Test
    void findEnabledPeriods_ShouldReturnEmptyList_WhenNoEnabledPeriods() {
        // Given - Delete all and create only disabled periods
        periodRepository.deleteAll();

        Period disabled1 = new Period();
        disabled1.setStartDate("2024-01-01");
        disabled1.setEndDate("2024-01-31");
        disabled1.setEnabled(false);

        Period disabled2 = new Period();
        disabled2.setStartDate("2024-02-01");
        disabled2.setEndDate("2024-02-28");
        disabled2.setEnabled(false);

        periodRepository.saveAll(Arrays.asList(disabled1, disabled2));

        // When
        List<Period> result = periodRepository.findEnabledPeriods();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_ShouldReturnPeriod_WhenIdExists() {
        // Given
        String periodId = period1.getId();

        // When
        Optional<Period> result = periodRepository.findById(periodId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(periodId, result.get().getId());
        assertEquals("2024-01-15", result.get().getStartDate());
        assertEquals("2024-05-15", result.get().getEndDate());
        assertTrue(result.get().getEnabled());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<Period> result = periodRepository.findById("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllPeriods() {
        // When
        List<Period> result = periodRepository.findAll();

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).extracting(Period::getStartDate)
                .containsExactlyInAnyOrder("2024-01-15", "2024-06-01", "2024-08-01", "2023-08-01");
    }

    @Test
    void save_ShouldPersistPeriodWithAllFields() {
        // Given
        Period newPeriod = new Period();
        newPeriod.setStartDate("2025-01-15");
        newPeriod.setEndDate("2025-05-15");
        newPeriod.setEnabled(false);

        // When
        Period savedPeriod = periodRepository.save(newPeriod);

        // Then
        assertNotNull(savedPeriod.getId());
        assertEquals("2025-01-15", savedPeriod.getStartDate());
        assertEquals("2025-05-15", savedPeriod.getEndDate());
        assertFalse(savedPeriod.getEnabled());

        // Verify it can be retrieved
        Optional<Period> retrievedPeriod = periodRepository.findById(savedPeriod.getId());
        assertTrue(retrievedPeriod.isPresent());
        assertEquals("2025-01-15", retrievedPeriod.get().getStartDate());
    }

    @Test
    void update_ShouldModifyExistingPeriod() {
        // Given
        Period periodToUpdate = period2; // This period is disabled
        periodToUpdate.setStartDate("2024-06-15"); // Change start date
        periodToUpdate.setEnabled(true); // Enable it

        // When
        Period updatedPeriod = periodRepository.save(periodToUpdate);

        // Then
        assertEquals(periodToUpdate.getId(), updatedPeriod.getId());
        assertEquals("2024-06-15", updatedPeriod.getStartDate());
        assertEquals("2024-07-31", updatedPeriod.getEndDate()); // Should remain unchanged
        assertTrue(updatedPeriod.getEnabled());

        // Verify it now appears in enabled periods
        List<Period> enabledPeriods = periodRepository.findEnabledPeriods();
        assertThat(enabledPeriods).extracting(Period::getId)
                .contains(updatedPeriod.getId());
    }

    @Test
    void delete_ShouldRemovePeriod() {
        // Given
        String periodId = period1.getId();

        // When
        periodRepository.deleteById(periodId);

        // Then
        Optional<Period> result = periodRepository.findById(periodId);
        assertFalse(result.isPresent());

        // Verify it's removed from enabled periods list
        List<Period> enabledPeriods = periodRepository.findEnabledPeriods();
        assertThat(enabledPeriods).extracting(Period::getId)
                .doesNotContain(periodId);
    }

    @Test
    void existsById_ShouldReturnTrueForExistingPeriod() {
        // When
        boolean exists = periodRepository.existsById(period1.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingPeriod() {
        // When
        boolean exists = periodRepository.existsById("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void count_ShouldReturnCorrectNumberOfPeriods() {
        // When
        long count = periodRepository.count();

        // Then
        assertEquals(4, count);
    }

    @Test
    void shouldHandleNullEnabledField() {
        // Given - Create period with null enabled field
        Period periodWithNullEnabled = new Period();
        periodWithNullEnabled.setStartDate("2025-01-01");
        periodWithNullEnabled.setEndDate("2025-01-31");
        periodWithNullEnabled.setEnabled(null);

        // When
        Period savedPeriod = periodRepository.save(periodWithNullEnabled);

        // Then
        assertNotNull(savedPeriod.getId());
        assertNull(savedPeriod.getEnabled());

        // Should not appear in enabled periods (null is not true)
        List<Period> enabledPeriods = periodRepository.findEnabledPeriods();
        assertThat(enabledPeriods).extracting(Period::getId)
                .doesNotContain(savedPeriod.getId());
    }

    @Test
    void shouldHandlePeriodWithOnlyRequiredFields() {
        // Given - Create period with only start and end dates
        Period minimalPeriod = new Period();
        minimalPeriod.setStartDate("2025-02-01");
        minimalPeriod.setEndDate("2025-02-28");
        // enabled field is not set (will be null)

        // When
        Period savedPeriod = periodRepository.save(minimalPeriod);

        // Then
        assertNotNull(savedPeriod.getId());
        assertEquals("2025-02-01", savedPeriod.getStartDate());
        assertEquals("2025-02-28", savedPeriod.getEndDate());
        assertNull(savedPeriod.getEnabled());
    }

    @Test
    void findEnabledPeriods_ShouldReturnCorrectCountAfterMultipleOperations() {
        // Given - Initial state has 3 enabled periods
        List<Period> initialEnabled = periodRepository.findEnabledPeriods();
        assertEquals(3, initialEnabled.size());

        // When - Disable one period
        period1.setEnabled(false);
        periodRepository.save(period1);

        // Then - Should have 2 enabled periods
        List<Period> afterDisable = periodRepository.findEnabledPeriods();
        assertEquals(2, afterDisable.size());

        // When - Enable a disabled period
        period2.setEnabled(true);
        periodRepository.save(period2);

        // Then - Should have 3 enabled periods again
        List<Period> afterEnable = periodRepository.findEnabledPeriods();
        assertEquals(3, afterEnable.size());

        // When - Delete an enabled period
        periodRepository.deleteById(period3.getId());

        // Then - Should have 2 enabled periods
        List<Period> afterDelete = periodRepository.findEnabledPeriods();
        assertEquals(2, afterDelete.size());
    }

    @Test
    void shouldHandleDuplicateDateRanges() {
        // Given - Create period with same dates as existing period
        Period duplicatePeriod = new Period();
        duplicatePeriod.setStartDate("2024-01-15"); // Same as period1
        duplicatePeriod.setEndDate("2024-05-15");   // Same as period1
        duplicatePeriod.setEnabled(true);

        // When
        Period savedDuplicate = periodRepository.save(duplicatePeriod);

        // Then - MongoDB allows duplicate date ranges
        assertNotNull(savedDuplicate.getId());
        assertEquals("2024-01-15", savedDuplicate.getStartDate());
        assertEquals("2024-05-15", savedDuplicate.getEndDate());

        // Both should exist in database
        List<Period> allPeriods = periodRepository.findAll();
        long countWithSameDates = allPeriods.stream()
                .filter(p -> "2024-01-15".equals(p.getStartDate()) && "2024-05-15".equals(p.getEndDate()))
                .count();
        assertEquals(2, countWithSameDates);

        // Both should appear in enabled periods
        List<Period> enabledPeriods = periodRepository.findEnabledPeriods();
        long enabledWithSameDates = enabledPeriods.stream()
                .filter(p -> "2024-01-15".equals(p.getStartDate()) && "2024-05-15".equals(p.getEndDate()))
                .count();
        assertEquals(2, enabledWithSameDates);
    }

    @Test
    void shouldHandleEmptyCollection() {
        // Given
        periodRepository.deleteAll();

        // When
        List<Period> allPeriods = periodRepository.findAll();
        List<Period> enabledPeriods = periodRepository.findEnabledPeriods();
        long count = periodRepository.count();

        // Then
        assertThat(allPeriods).isEmpty();
        assertThat(enabledPeriods).isEmpty();
        assertEquals(0, count);
    }

    @Test
    void shouldMaintainDataConsistency() {
        // When - Perform multiple operations
        List<Period> initialEnabled = periodRepository.findEnabledPeriods();
        long initialCount = periodRepository.count();

        // Disable a period
        period1.setEnabled(false);
        periodRepository.save(period1);

        // Create a new period
        Period newPeriod = new Period();
        newPeriod.setStartDate("2025-01-01");
        newPeriod.setEndDate("2025-06-01");
        newPeriod.setEnabled(true);
        periodRepository.save(newPeriod);

        // Delete a period
        periodRepository.deleteById(period2.getId());

        // Then - Verify final state
        List<Period> finalEnabled = periodRepository.findEnabledPeriods();
        long finalCount = periodRepository.count();

        // We started with 3 enabled, disabled 1, added 1 new enabled, deleted 1 disabled
        // So enabled count should be: 3 - 1 + 1 = 3
        assertEquals(3, finalEnabled.size());

        // Total count should be: 4 - 1 + 1 = 4
        assertEquals(4, finalCount);
    }
}