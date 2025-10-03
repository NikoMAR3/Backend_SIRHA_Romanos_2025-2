package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule schedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        schedule = new Schedule();
        schedule.setId("1");
        schedule.setStudentId("123");
        schedule.setSubjectShortName("MAT101");
        schedule.setName("Matemáticas");
        schedule.setSemester(1);
        schedule.setCredits(3);
    }

    @Test
    void createSchedule_ShouldSaveAndReturnSchedule() {
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        Schedule created = scheduleService.createSchedule(schedule);

        assertNotNull(created);
        assertEquals("123", created.getStudentId());
        verify(scheduleRepository, times(1)).save(schedule);
    }

    @Test
    void createSchedule_ShouldThrowException_WhenNull() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createSchedule(null));
    }

    @Test
    void createSchedule_ShouldThrowException_WhenStudentIdEmpty() {
        schedule.setStudentId("");
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createSchedule(schedule));
    }

    @Test
    void deleteSchedule_ShouldReturnTrue_WhenExists() {
        when(scheduleRepository.existsById("1")).thenReturn(true);
        doNothing().when(scheduleRepository).deleteById("1");

        boolean result = scheduleService.deleteSchedule("1");

        assertTrue(result);
        verify(scheduleRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteSchedule_ShouldReturnFalse_WhenNotExists() {
        when(scheduleRepository.existsById("1")).thenReturn(false);

        boolean result = scheduleService.deleteSchedule("1");

        assertFalse(result);
        verify(scheduleRepository, never()).deleteById(anyString());
    }

    @Test
    void updateSchedule_ShouldUpdateFields() {
        Schedule existing = new Schedule();
        existing.setId("1");
        existing.setStudentId("999");

        when(scheduleRepository.findById("1")).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        Schedule updated = scheduleService.updateSchedule(schedule);

        assertEquals("123", updated.getStudentId());
        verify(scheduleRepository, times(1)).save(existing);
    }

    @Test
    void updateSchedule_ShouldThrow_WhenScheduleNotFound() {
        when(scheduleRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> scheduleService.updateSchedule(schedule));
    }

    @Test
    void searchScheduleByStudentId_ShouldReturnSchedule() {
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.of(schedule));

        Schedule result = scheduleService.searchScheduleByStudentId("123");

        assertNotNull(result);
        assertEquals("123", result.getStudentId());
    }

    @Test
    void searchScheduleByStudentId_ShouldThrow_WhenNotFound() {
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> scheduleService.searchScheduleByStudentId("123"));
    }

    @Test
    void checkScheduleConflicts_ShouldReturnTrue_WhenOverlapExists() {
        ClassSession class1 = new ClassSession();
        class1.setStartDate(LocalDateTime.of(2025, 10, 1, 8, 0));
        class1.setEndDate(LocalDateTime.of(2025, 10, 1, 10, 0));

        ClassSession class2 = new ClassSession();
        class2.setStartDate(LocalDateTime.of(2025, 10, 1, 9, 0));
        class2.setEndDate(LocalDateTime.of(2025, 10, 1, 11, 0));

        Schedule existing = new Schedule();
        existing.setClassSessions(List.of(class1));

        Subject subject = new Subject();
        subject.setId(String.valueOf(1));
        subject.setName("Math");

        schedule.setClassSessions(List.of(class2));
        schedule.setSubjects(List.of(subject));

        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.of(existing));

        boolean conflict = scheduleService.checkScheduleConflicts(123, schedule);

        assertTrue(conflict);
    }

    @Test
    void checkScheduleConflicts_ShouldReturnFalse_WhenNoOverlap() {
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.empty());

        boolean conflict = scheduleService.checkScheduleConflicts(123, schedule);

        assertFalse(conflict);
    }

    @Test
    void validateScheduleCapacity_ShouldReturnTrue_WhenCapacityAvailable() {
        ClassSession classSession = new ClassSession();
        classSession.setId("1");
        classSession.setCapacity(50);

        when(scheduleRepository.countBySubjectId(1)).thenReturn(10L);

        boolean result = scheduleService.validateScheduleCapacity(classSession);

        assertTrue(result);
    }

    @Test
    void validateScheduleCapacity_ShouldReturnFalse_WhenCapacityExceeded() {
        ClassSession classSession = new ClassSession();
        classSession.setId("1");
        classSession.setCapacity(10);

        when(scheduleRepository.countBySubjectId(1)).thenReturn(15L);

        boolean result = scheduleService.validateScheduleCapacity(classSession);

        assertFalse(result);
    }
}
