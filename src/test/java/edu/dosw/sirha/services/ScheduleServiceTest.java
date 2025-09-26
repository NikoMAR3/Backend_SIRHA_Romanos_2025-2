package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Schedule;
import edu.dosw.sirha.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for testing the functionality of the ScheduleService class.
 * This test class verifies proper schedule management operations including
 * schedule addition, retrieval, class checking, and class session access.
 * Tests utilize reflection for accessing private fields and Mockito framework
 * for mock object creation and behavior verification using JUnit 5 framework.
 */
class ScheduleServiceTest {
    private ScheduleService scheduleService;
    private Student mockStudent;
    private Schedule mockSchedule;
    private ClassSession mockClassSession;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes a ScheduleService instance and creates mock objects for
     * Student, Schedule, and ClassSession for use across multiple test methods,
     * ensuring isolated test execution and consistent mock behavior.
     */
    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService();
        mockStudent = mock(Student.class);
        mockSchedule = mock(Schedule.class);
        mockClassSession = mock(ClassSession.class);
    }

    /**
     * Tests the addSchedule method for schedule addition functionality.
     * Verifies that when a schedule is added for a specific student,
     * it is correctly stored in the manager's internal data structure
     * and can be retrieved using the student as the key.
     */
    @Test
    void testAddSchedule() {
        scheduleService.addSchedule(mockStudent, mockSchedule);
        assertEquals(mockSchedule, scheduleService.getSchedules().get(mockStudent));
    }

    /**
     * Tests the checkSchedule method for schedule retrieval.
     * Verifies that when a schedule exists for a student in the manager,
     * it can be correctly retrieved using the checkSchedule method,
     * ensuring proper schedule lookup functionality.
     */
    @Test
    void testCheckSchedule() {
        scheduleService.getSchedules().put(mockStudent, mockSchedule);
        Schedule result = scheduleService.checkSchedule(mockStudent);
        assertEquals(mockSchedule, result);
    }

    /**
     * Tests the checkClassInSchedule method for class presence verification.
     * Verifies that the manager can correctly determine whether a specific
     * class (by name) exists in a student's schedule by delegating to the
     * schedule's containsClass method and returning the appropriate boolean result.
     */
    @Test
    void testCheckClassInSchedule() {
        String className = "math";
        when(mockSchedule.containsClass(className)).thenReturn(true);
        scheduleService.getSchedules().put(mockStudent, mockSchedule);
        Boolean result = scheduleService.checkClassInSchedule(className, mockStudent);
        assertTrue(result);
    }

    /**
     * Tests the getClassInSchedule method for class session retrieval.
     * Verifies that the manager can retrieve all class sessions associated
     * with a student's schedule, returning the complete list of classes
     * from the schedule's getClasses method.
     */
    @Test
    void testGetClassInSchedule() {
        ArrayList<ClassSession> classes = new ArrayList<>();
        classes.add(mockClassSession);
        when(mockSchedule.getClasses()).thenReturn(classes);
        scheduleService.getSchedules().put(mockStudent, mockSchedule);
        ArrayList<ClassSession> result = scheduleService.getClassInSchedule(mockStudent);
        assertEquals(classes, result);
    }

    /**
     * Utility method for accessing the private schedules field using reflection.
     * This method provides access to the internal HashMap storage of ScheduleService
     * for testing purposes, allowing direct manipulation and verification of
     * the internal state without relying solely on public interface methods.
     *
     * @return HashMap containing the student-schedule mappings managed by the ScheduleService
     * @throws RuntimeException if reflection access fails or field is not found
     */
    private HashMap<Student, Schedule> getSchedules() {
        try {
            java.lang.reflect.Field field = ScheduleService.class.getDeclaredField("schedules");
            field.setAccessible(true);
            return (HashMap<Student, Schedule>) field.get(scheduleService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
