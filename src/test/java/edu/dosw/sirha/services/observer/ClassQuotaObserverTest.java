package edu.dosw.sirha.services.observer;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.services.observer.ClassQuotaObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit test class for testing the functionality of the ClassQuotaObserver class.
 * This test class verifies proper alert generation, event handling, observer pattern
 * implementation, and alert management operations in response to class session
 * quota-related events using JUnit 5 and Mockito framework.
 */
class ClassQuotaObserverTest {
    private ClassQuotaObserver observer;
    @Mock
    private ClassSession mockClassSession;
    @Mock
    private Student mockStudent;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes mock objects and configures the ClassQuotaObserver instance
     * with predefined mock ClassSession behavior for consistent testing across
     * all test methods.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        observer = new ClassQuotaObserver();

        when(mockClassSession.getId()).thenReturn("TEST-01");
        when(mockClassSession.getProfessor()).thenReturn("Prof. Test");
        when(mockClassSession.getMaxQuota()).thenReturn(10);
        when(mockClassSession.getCurrentQuota()).thenReturn(5);
    }

    /**
     * Tests the quota full event handling.
     * Verifies that when a QUOTA_FULL event is received, the observer
     * generates a critical alert with the correct type, level, and message
     * containing the class session identifier.
     */
    @Test
    void testUpdateQuotaFullEvent() {
        observer.update(mockClassSession, "QUOTA_FULL", null);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("QUOTA_FULL", alert.getType());
        assertEquals("CRITICAL", alert.getLevel());
        assertTrue(alert.getMessage().contains("TEST-01"));
    }

    /**
     * Tests the quota warning event handling.
     * Verifies that when a QUOTA_90_PERCENT event is received, the observer
     * generates a warning alert with the correct type, level, and message
     * containing the percentage threshold information.
     */
    @Test
    void testUpdateQuotaWarningEvent() {
        observer.update(mockClassSession, "QUOTA_90_PERCENT", 90.0);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("QUOTA_WARNING", alert.getType());
        assertEquals("WARNING", alert.getLevel());
        assertTrue(alert.getMessage().contains("90%"));
    }

    /**
     * Tests the student added event handling.
     * Verifies that when a STUDENT_ADDED event is received, the observer
     * generates an informational alert with the correct type, level, and
     * message containing the student identifier.
     */
    @Test
    void testUpdateStudentAddedEvent() {
        when(mockStudent.getId()).thenReturn("202012345");

        observer.update(mockClassSession, "STUDENT_ADDED", mockStudent);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("STUDENT_ADDED", alert.getType());
        assertEquals("INFO", alert.getLevel());
        assertTrue(alert.getMessage().contains("202012345"));
    }

    /**
     * Tests the handling of unknown or unrecognized events.
     * Verifies that when an unknown event type is received, the observer
     * generates a general alert containing the event name, ensuring
     * graceful handling of unexpected events.
     */
    @Test
    void testUpdateUnknownEvent() {
        observer.update(mockClassSession, "UNKNOWN_EVENT", null);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("GENERAL", alert.getType());
        assertTrue(alert.getMessage().contains("UNKNOWN_EVENT"));
    }

    /**
     * Tests the alert clearing functionality.
     * Verifies that the clearAlerts method properly removes all
     * previously generated alerts from the observer's alert collection,
     * resetting it to an empty state.
     */
    @Test
    void testClearAlerts() {
        observer.update(mockClassSession, "QUOTA_FULL", null);
        observer.update(mockClassSession, "STUDENT_ADDED", mockStudent);
        assertEquals(2, observer.getAlerts().size());

        observer.clearAlerts();

        assertEquals(0, observer.getAlerts().size());
    }

    /**
     * Tests the correct ordering of multiple events and alerts.
     * Verifies that when multiple events are processed in sequence,
     * the alerts are stored in the same order they were received,
     * ensuring proper chronological tracking of events.
     */
    @Test
    void testMultipleEventsCorrectOrder() {
        observer.update(mockClassSession, "STUDENT_ADDED", mockStudent);
        observer.update(mockClassSession, "QUOTA_90_PERCENT", 90.0);
        observer.update(mockClassSession, "QUOTA_FULL", null);

        assertEquals(3, observer.getAlerts().size());

        assertEquals("STUDENT_ADDED", observer.getAlerts().get(0).getType());
        assertEquals("QUOTA_WARNING", observer.getAlerts().get(1).getType());
        assertEquals("QUOTA_FULL", observer.getAlerts().get(2).getType());
    }
}
