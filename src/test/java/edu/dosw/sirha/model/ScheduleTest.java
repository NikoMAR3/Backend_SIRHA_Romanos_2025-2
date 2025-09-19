package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
    }

    @Test
    void testConstructor() {
        assertNotNull(schedule.getClasses());
        assertTrue(schedule.getClasses().isEmpty());
    }

    @Test
    void testGetClasses() {
        ClassSession cs1 = new ClassSession("CS101", "Laura", LocalDateTime.now(), 30);
        ClassSession cs2 = new ClassSession("CS102", "Martin", LocalDateTime.now(), 25);

        schedule.getClasses().add(cs1);
        schedule.getClasses().add(cs2);

        assertEquals(2, schedule.getClasses().size());
        assertTrue(schedule.getClasses().contains(cs1));
        assertTrue(schedule.getClasses().contains(cs2));
    }

    @Test
    void testGetClassesIsArrayList() {
        assertTrue(schedule.getClasses() instanceof java.util.ArrayList);
    }
}
