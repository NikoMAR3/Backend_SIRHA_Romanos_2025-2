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

class ScheduleManagerTest {
    private ScheduleManager scheduleManager;
    private Student mockStudent;
    private Schedule mockSchedule;
    private ClassSession mockClassSession;

    @BeforeEach
    void setUp() {
        scheduleManager = new ScheduleManager();
        mockStudent = mock(Student.class);
        mockSchedule = mock(Schedule.class);
        mockClassSession = mock(ClassSession.class);
    }

    @Test
    void testAddSchedule() {
        scheduleManager.addSchedule(mockStudent, mockSchedule);
        assertEquals(mockSchedule, scheduleManager.getSchedules().get(mockStudent));
    }

    @Test
    void testCheckSchedule() {
        scheduleManager.getSchedules().put(mockStudent, mockSchedule);
        Schedule result = scheduleManager.checkSchedule(mockStudent);
        assertEquals(mockSchedule, result);
    }

    @Test
    void testCheckClassInSchedule() {
        String className = "math";
        when(mockSchedule.containsClass(className)).thenReturn(true);
        scheduleManager.getSchedules().put(mockStudent, mockSchedule);
        Boolean result = scheduleManager.checkClassInSchedule(className, mockStudent);
        assertTrue(result);
    }

    @Test
    void testGetClassInSchedule() {
        ArrayList<ClassSession> classes = new ArrayList<>();
        classes.add(mockClassSession);
        when(mockSchedule.getClasses()).thenReturn(classes);
        scheduleManager.getSchedules().put(mockStudent, mockSchedule);
        ArrayList<ClassSession> result = scheduleManager.getClassInSchedule(mockStudent);
        assertEquals(classes, result);
    }


    private HashMap<Student, Schedule> getSchedules() {
        try {
            java.lang.reflect.Field field = ScheduleManager.class.getDeclaredField("schedules");
            field.setAccessible(true);
            return (HashMap<Student, Schedule>) field.get(scheduleManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}