package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing the functionality of the Schedule class.
 * This test class verifies the proper initialization, data retrieval,
 * and collection management of Schedule instances.
 */
class ScheduleTest {
    private Schedule schedule;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes a new Schedule instance for testing.
     */
    @BeforeEach
    void setUp() {
        schedule = new Schedule();
    }

    /**
     * Tests the Schedule constructor to ensure proper initialization.
     * Verifies that the classes collection is not null and is initially empty.
     */
    @Test
    void testConstructor() {
        assertNotNull(schedule.getClasses());
        assertTrue(schedule.getClasses().isEmpty());
    }

    /**
     * Tests the getClasses method functionality.
     * Verifies that class sessions can be added to the schedule
     * and properly retrieved through the getClasses method.
     * Tests collection size and contains operations.
     */
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

    /**
     * Tests that the getClasses method returns an ArrayList instance.
     * Verifies the specific implementation type of the classes collection.
     */
    @Test
    void testGetClassesIsArrayList() {
        assertTrue(schedule.getClasses() instanceof java.util.ArrayList);
    }
}
