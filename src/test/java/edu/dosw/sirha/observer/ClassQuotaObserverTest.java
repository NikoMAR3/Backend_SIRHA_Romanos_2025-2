package edu.dosw.sirha.observer;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ClassQuotaObserverTest {
    private ClassQuotaObserver observer;
    @Mock
    private ClassSession mockClassSession;
    @Mock
    private Student mockStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        observer = new ClassQuotaObserver();

        when(mockClassSession.getId()).thenReturn("TEST-01");
        when(mockClassSession.getProfessor()).thenReturn("Prof. Test");
        when(mockClassSession.getMaxQuota()).thenReturn(10);
        when(mockClassSession.getCurrentQuota()).thenReturn(5);
    }

    @Test
    void testUpdateQuotaFullEvent() {
        observer.update(mockClassSession, "QUOTA_FULL", null);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("QUOTA_FULL", alert.getType());
        assertEquals("CRITICAL", alert.getLevel());
        assertTrue(alert.getMessage().contains("TEST-01"));
    }

    @Test
    void testUpdateQuotaWarningEvent() {
        observer.update(mockClassSession, "QUOTA_90_PERCENT", 90.0);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("QUOTA_WARNING", alert.getType());
        assertEquals("WARNING", alert.getLevel());
        assertTrue(alert.getMessage().contains("90%"));
    }

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

    @Test
    void testUpdateUnknownEvent() {
        observer.update(mockClassSession, "UNKNOWN_EVENT", null);

        assertEquals(1, observer.getAlerts().size());
        Alert alert = observer.getAlerts().get(0);
        assertEquals("GENERAL", alert.getType());
        assertTrue(alert.getMessage().contains("UNKNOWN_EVENT"));
    }

    @Test
    void testClearAlerts() {
        observer.update(mockClassSession, "QUOTA_FULL", null);
        observer.update(mockClassSession, "STUDENT_ADDED", mockStudent);
        assertEquals(2, observer.getAlerts().size());

        observer.clearAlerts();

        assertEquals(0, observer.getAlerts().size());
    }

    @Test
    void testGetAlertsReturnsImmutableCopy() {
        observer.update(mockClassSession, "QUOTA_FULL", null);

        var alerts = observer.getAlerts();

        assertThrows(UnsupportedOperationException.class, () -> {
            alerts.add(null);
        });

        assertEquals(1, observer.getAlerts().size());
    }

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