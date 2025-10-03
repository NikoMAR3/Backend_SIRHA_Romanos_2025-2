package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Period;
import edu.dosw.sirha.model.persistence.repository.PeriodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodServiceTest {
    @Mock
    private PeriodRepository periodRepository;
    @InjectMocks
    private PeriodService periodService;
    private Period validPeriod;

    @BeforeEach
    void setUp() {
        validPeriod = new Period();
        validPeriod.setId("1");
        validPeriod.setStartDate("2025-01-01");
        validPeriod.setEndDate("2025-06-30");
        validPeriod.setEnabled(true);
    }

    @Test
    void createPeriod_ShouldSave_WhenValidPeriod() {
        when(periodRepository.save(validPeriod)).thenReturn(validPeriod);

        Period result = periodService.createPeriod(validPeriod);

        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(periodRepository).save(validPeriod);
    }

    @Test
    void createPeriod_ShouldThrow_WhenNull() {
        assertThrows(IllegalArgumentException.class, () -> periodService.createPeriod(null));
    }

    @Test
    void createPeriod_ShouldThrow_WhenStartAfterEnd() {
        Period invalid = new Period();
        invalid.setStartDate("2025-07-01");
        invalid.setEndDate("2025-01-01");

        assertThrows(IllegalArgumentException.class, () -> periodService.createPeriod(invalid));
    }

    @Test
    void modifyPeriod_ShouldSave_WhenValidPeriod() {
        when(periodRepository.existsById("1")).thenReturn(true);
        when(periodRepository.save(validPeriod)).thenReturn(validPeriod);

        Period result = periodService.modifyPeriod(validPeriod);

        assertEquals("1", result.getId());
        verify(periodRepository).save(validPeriod);
    }

    @Test
    void modifyPeriod_ShouldThrow_WhenNoId() {
        Period withoutId = new Period();
        withoutId.setStartDate("2025-01-01");
        withoutId.setEndDate("2025-06-30");

        assertThrows(IllegalArgumentException.class, () -> periodService.modifyPeriod(withoutId));
    }

    @Test
    void modifyPeriod_ShouldThrow_WhenNotExists() {
        when(periodRepository.existsById("1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> periodService.modifyPeriod(validPeriod));
    }

    @Test
    void deletePeriod_ShouldDelete_WhenExists() {
        when(periodRepository.existsById("1")).thenReturn(true);

        periodService.deletePeriod("1");

        verify(periodRepository).deleteById("1");
    }

    @Test
    void deletePeriod_ShouldThrow_WhenNotExists() {
        when(periodRepository.existsById("1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> periodService.deletePeriod("1"));
    }

    @Test
    void searchPeriodById_ShouldReturn_WhenExists() {
        when(periodRepository.findById("1")).thenReturn(Optional.of(validPeriod));

        Period result = periodService.searchPeriodById("1");

        assertEquals("1", result.getId());
    }

    @Test
    void searchPeriodById_ShouldThrow_WhenNotFound() {
        when(periodRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> periodService.searchPeriodById("1"));
    }

    @Test
    void checkEnabledPeriods_ShouldReturnList() {
        when(periodRepository.findEnabledPeriods()).thenReturn(Arrays.asList(validPeriod));

        List<Period> result = periodService.checkEnabledPeriods();

        assertEquals(1, result.size());
    }

    @Test
    void searchAllPeriods_ShouldReturnAll() {
        when(periodRepository.findAll()).thenReturn(Arrays.asList(validPeriod));

        List<Period> result = periodService.searchAllPeriods();

        assertFalse(result.isEmpty());
    }

    @Test
    void togglePeriodStatus_ShouldUpdateEnabled() {
        when(periodRepository.findById("1")).thenReturn(Optional.of(validPeriod));
        when(periodRepository.save(any(Period.class))).thenReturn(validPeriod);

        periodService.togglePeriodStatus("1", false);

        verify(periodRepository).save(validPeriod);
        assertFalse(validPeriod.getEnabled());
    }

    @Test
    void togglePeriodStatus_ShouldThrow_WhenNotExists() {
        when(periodRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> periodService.togglePeriodStatus("1", false));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(periodRepository.existsById("1")).thenReturn(true);

        assertTrue(periodService.existsById("1"));
    }

    @Test
    void existsById_ShouldThrow_WhenIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> periodService.existsById("   "));
    }
}

