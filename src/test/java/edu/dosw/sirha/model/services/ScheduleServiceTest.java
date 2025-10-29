package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.Professor;
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
    private ClassSession classSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        schedule = new Schedule();
        schedule.setId("1");
        schedule.setStudentId("123");
        schedule.setSubjectShortName("MAT101");
        schedule.setName("Matemáticas");
        schedule.setSemester("2024-1");
        schedule.setCredits(3);
        schedule.setClassSessions(new ArrayList<>());
        classSession = new ClassSession();
        classSession.setId("cs1");
        classSession.setStartDate(LocalDateTime.of(2025, 10, 1, 8, 0));
        classSession.setEndDate(LocalDateTime.of(2025, 10, 1, 10, 0));
        classSession.setCapacity(30);
    }

    // --- Create ---
    @Test
    void createSchedule_ShouldSaveAndReturnSchedule() {
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        assertEquals(schedule, scheduleService.createSchedule(schedule));
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

    // --- Delete ---
    @Test
    void deleteSchedule_ShouldReturnTrue_WhenExists() {
        when(scheduleRepository.existsById("1")).thenReturn(true);
        doNothing().when(scheduleRepository).deleteById("1");
        assertTrue(scheduleService.deleteSchedule("1"));
    }

    @Test
    void deleteSchedule_ShouldReturnFalse_WhenNotExists() {
        when(scheduleRepository.existsById("1")).thenReturn(false);
        assertFalse(scheduleService.deleteSchedule("1"));
    }

    @Test
    void deleteSchedule_ShouldThrowException_WhenIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.deleteSchedule(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.deleteSchedule(null));
    }

    // --- Update ---
    @Test
    void updateSchedule_ShouldUpdateFields() {
        Schedule existing = new Schedule();
        existing.setId("1");
        existing.setStudentId("999");
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(existing)).thenReturn(existing);

        schedule.setClassSessions(List.of(classSession));
        schedule.setCredits(10);
        schedule.setProgram("programaA");
        Schedule updated = scheduleService.updateSchedule(schedule);
        assertEquals("123", updated.getStudentId());
        assertEquals(10, updated.getCredits());
    }

    @Test
    void updateSchedule_ShouldThrow_WhenNullSchedule() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.updateSchedule(null));
    }

    @Test
    void updateSchedule_ShouldThrow_WhenNoId() {
        schedule.setId(null);
        assertThrows(IllegalArgumentException.class, () -> scheduleService.updateSchedule(schedule));
    }

    @Test
    void updateSchedule_ShouldThrow_WhenScheduleNotFound() {
        when(scheduleRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.updateSchedule(schedule));
    }

    // --- Search by studentId ---
    @Test
    void searchScheduleByStudentId_ShouldReturnSchedule() {
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.of(schedule));
        assertEquals(schedule, scheduleService.searchScheduleByStudentId("123"));
    }

    @Test
    void searchScheduleByStudentId_ShouldThrow_WhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchScheduleByStudentId(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchScheduleByStudentId(null));
    }

    @Test
    void searchScheduleByStudentId_ShouldThrow_WhenNotFound() {
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.searchScheduleByStudentId("123"));
    }

    // --- searchAllSchedules ---
    @Test
    void searchAllSchedules_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findAll()).thenReturn(schedules);
        assertEquals(schedules, scheduleService.searchAllSchedules());
    }

    // --- searchScheduleBySubject ---
    @Test
    void searchScheduleBySubject_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findBySubjectId("subj1")).thenReturn(schedules);
        assertEquals(schedules, scheduleService.searchScheduleBySubject("subj1"));
    }

    // --- searchScheduleBySubjectShortName ---
    @Test
    void searchScheduleBySubjectShortName_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findBySubjectShortName("MAT101")).thenReturn(schedules);
        assertEquals(schedules, scheduleService.searchScheduleBySubjectShortName("MAT101"));
    }

    @Test
    void searchScheduleBySubjectShortName_ShouldThrow_WhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchScheduleBySubjectShortName(null));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchScheduleBySubjectShortName(""));
    }

    // --- searchScheduleByName ---
    @Test
    void searchScheduleByName_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findBySubjectName("Matemáticas")).thenReturn(schedules);
        assertEquals(schedules, scheduleService.searchScheduleByName("Matemáticas"));
    }

    @Test
    void searchScheduleByName_ShouldThrow_WhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchScheduleByName(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchScheduleByName(null));
    }

    // --- searchByScheduleBySemester ---
    @Test
    void searchByScheduleBySemester_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findBySemester("2024-1")).thenReturn(schedules);
        assertEquals(schedules, scheduleService.searchByScheduleBySemester("2024-1"));
    }

    @Test
    void searchByScheduleBySemester_ShouldThrow_WhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchByScheduleBySemester(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchByScheduleBySemester(null));
    }

    // --- searchByScheduleByProgram ---
    @Test
    void searchByScheduleByProgram_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findByProgram("prog")).thenReturn(schedules);
        assertEquals(schedules, scheduleService.searchByScheduleByProgram("prog"));
    }

    @Test
    void searchByScheduleByProgram_ShouldThrow_WhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchByScheduleByProgram(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.searchByScheduleByProgram(null));
    }

    // --- checkScheduleConflicts ---
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
        schedule.setClassSessions(List.of(class2));
        schedule.setSubjects(List.of(new Subject()));
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.of(existing));
        assertTrue(scheduleService.checkScheduleConflicts(123, schedule));
    }

    @Test
    void checkScheduleConflicts_ShouldReturnFalse_WhenNoOverlap() {
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.empty());
        assertFalse(scheduleService.checkScheduleConflicts(123, schedule));
    }

    @Test
    void checkScheduleConflicts_ShouldReturnFalse_WhenSubjectsNullOrEmpty() {
        Schedule existing = new Schedule();
        existing.setClassSessions(List.of(classSession));
        schedule.setClassSessions(List.of(classSession));
        schedule.setSubjects(null);
        when(scheduleRepository.findByStudentId("123")).thenReturn(Optional.of(existing));
        assertFalse(scheduleService.checkScheduleConflicts(123, schedule));
        schedule.setSubjects(Collections.emptyList());
        assertFalse(scheduleService.checkScheduleConflicts(123, schedule));
    }

    @Test
    void checkScheduleConflicts_ShouldThrow_WhenStudentIdInvalidOrNullSchedule() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.checkScheduleConflicts(0, schedule));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.checkScheduleConflicts(123, null));
    }

    // --- validateScheduleCapacity ---
    @Test
    void validateScheduleCapacity_ShouldReturnTrue_WhenCapacityAvailable() {
        classSession.setId("cs1");
        classSession.setCapacity(30);
        when(scheduleRepository.countBySubjectId("cs1")).thenReturn(10L);
        assertTrue(scheduleService.validateScheduleCapacity(classSession));
    }

    @Test
    void validateScheduleCapacity_ShouldReturnFalse_WhenCapacityExceeded() {
        classSession.setId("cs1");
        classSession.setCapacity(10);
        when(scheduleRepository.countBySubjectId("cs1")).thenReturn(15L);
        assertFalse(scheduleService.validateScheduleCapacity(classSession));
    }

    @Test
    void validateScheduleCapacity_ShouldThrow_WhenNull() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.validateScheduleCapacity(null));
    }

    // --- getCurrentSchedule ---
    @Test
    void getCurrentSchedule_ShouldReturnSchedule() {
        when(scheduleRepository.findCurrentScheduleByStudentId("123")).thenReturn(Optional.of(schedule));
        assertEquals(schedule, scheduleService.getCurrentSchedule("123"));
    }

    @Test
    void getCurrentSchedule_ShouldThrow_WhenNotFoundOrInvalid() {
        when(scheduleRepository.findCurrentScheduleByStudentId("123")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.getCurrentSchedule("123"));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getCurrentSchedule(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getCurrentSchedule(null));
    }

    // --- getScheduleHistory ---
    @Test
    void getScheduleHistory_ShouldReturnList() {
        List<Schedule> schedules = List.of(schedule);
        when(scheduleRepository.findScheduleHistoryByStudentId("123")).thenReturn(schedules);
        assertEquals(schedules, scheduleService.getScheduleHistory("123"));
    }

    @Test
    void getScheduleHistory_ShouldThrow_WhenInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getScheduleHistory(""));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getScheduleHistory(null));
    }

    // --- addClassSessionToSchedule ---
    @Test
    void addClassSessionToSchedule_ShouldAdd_WhenNoConflict() {
        Schedule scheduleWithSessions = new Schedule();
        scheduleWithSessions.setId("1");
        scheduleWithSessions.setClassSessions(new ArrayList<>());
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(scheduleWithSessions));
        when(scheduleRepository.save(scheduleWithSessions)).thenReturn(scheduleWithSessions);
        when(scheduleRepository.countBySubjectId("cs1")).thenReturn(0L);
        assertEquals(scheduleWithSessions, scheduleService.addClassSessionToSchedule("1", classSession));
        assertTrue(scheduleWithSessions.getClassSessions().contains(classSession));
    }

    @Test
    void addClassSessionToSchedule_ShouldThrow_WhenIdOrClassSessionInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.addClassSessionToSchedule(null, classSession));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.addClassSessionToSchedule("1", null));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.addClassSessionToSchedule("", classSession));
    }

    @Test
    void addClassSessionToSchedule_ShouldThrow_WhenScheduleNotFound() {
        when(scheduleRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.addClassSessionToSchedule("1", classSession));
    }

    @Test
    void addClassSessionToSchedule_ShouldThrow_WhenConflict() {
        Schedule s = new Schedule();
        s.setId("1");
        ClassSession sessionInSchedule = new ClassSession();
        sessionInSchedule.setStartDate(classSession.getStartDate());
        sessionInSchedule.setEndDate(classSession.getEndDate());
        s.setClassSessions(new ArrayList<>(List.of(sessionInSchedule)));
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        when(scheduleRepository.countBySubjectId(any())).thenReturn(0L);
        classSession.setStartDate(sessionInSchedule.getStartDate());
        classSession.setEndDate(sessionInSchedule.getEndDate());
        assertThrows(RuntimeException.class, () -> scheduleService.addClassSessionToSchedule("1", classSession));
    }

    @Test
    void addClassSessionToSchedule_ShouldThrow_WhenCapacityFull() {
        Schedule s = new Schedule();
        s.setId("1");
        s.setClassSessions(new ArrayList<>());
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        when(scheduleRepository.countBySubjectId("cs1")).thenReturn(99L);
        classSession.setCapacity(10);
        assertThrows(RuntimeException.class, () -> scheduleService.addClassSessionToSchedule("1", classSession));
    }

    // --- removeClassSessionFromSchedule ---
    @Test
    void removeClassSessionFromSchedule_ShouldRemove() {
        ClassSession cs = new ClassSession();
        cs.setId("rm1");
        Schedule s = new Schedule();
        s.setId("1");
        s.setClassSessions(new ArrayList<>(List.of(cs)));
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        when(scheduleRepository.save(s)).thenReturn(s);
        assertEquals(s, scheduleService.removeClassSessionFromSchedule("1", "rm1"));
        assertTrue(s.getClassSessions().isEmpty());
    }

    @Test
    void removeClassSessionFromSchedule_ShouldThrow_WhenIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.removeClassSessionFromSchedule("", "rm1"));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.removeClassSessionFromSchedule("1", null));
    }

    @Test
    void removeClassSessionFromSchedule_ShouldThrow_WhenScheduleNotFound() {
        when(scheduleRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.removeClassSessionFromSchedule("1", "rm1"));
    }

    @Test
    void removeClassSessionFromSchedule_ShouldThrow_WhenNoClassSessions() {
        Schedule s = new Schedule();
        s.setId("1");
        s.setClassSessions(new ArrayList<>());
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        assertThrows(RuntimeException.class, () -> scheduleService.removeClassSessionFromSchedule("1", "rm1"));
    }

    @Test
    void removeClassSessionFromSchedule_ShouldThrow_WhenClassNotFound() {
        ClassSession cs = new ClassSession();
        cs.setId("rm2");
        Schedule s = new Schedule();
        s.setId("1");
        s.setClassSessions(new ArrayList<>(List.of(cs)));
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        assertThrows(RuntimeException.class, () -> scheduleService.removeClassSessionFromSchedule("1", "rm1"));
    }

    // --- getClassSessionsBySchedule ---
    @Test
    void getClassSessionsBySchedule_ShouldReturnList() {
        Schedule s = new Schedule();
        s.setId("1");
        List<ClassSession> cs = List.of(classSession);
        s.setClassSessions(cs);
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        assertEquals(cs, scheduleService.getClassSessionsBySchedule("1"));
    }

    @Test
    void getClassSessionsBySchedule_ShouldThrow_WhenInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getClassSessionsBySchedule(""));
        when(scheduleRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.getClassSessionsBySchedule("1"));
    }

    // --- getClassSessionById ---
    @Test
    void getClassSessionById_ShouldReturnClassSession() {
        ClassSession cs = new ClassSession();
        cs.setId("cs1");
        Schedule s = new Schedule();
        s.setId("1");
        s.setClassSessions(List.of(cs));
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        assertEquals(cs, scheduleService.getClassSessionById("1", "cs1"));
    }

    @Test
    void getClassSessionById_ShouldThrow_WhenInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getClassSessionById("", "cs1"));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.getClassSessionById("1", ""));
        when(scheduleRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduleService.getClassSessionById("1", "cs1"));
        // Not found in list
        Schedule s = new Schedule();
        s.setId("1");
        s.setClassSessions(new ArrayList<>());
        when(scheduleRepository.findById("1")).thenReturn(Optional.of(s));
        assertThrows(RuntimeException.class, () -> scheduleService.getClassSessionById("1", "cs1"));
    }

    // --- hasTimeConflict (indirectly covered above) ---

}