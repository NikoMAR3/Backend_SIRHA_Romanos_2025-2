package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.services.observer.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit test class for testing the functionality of the QuotaAlertFactory class.
 * This test class verifies proper alert creation for different quota-related
 * scenarios including quota full, quota warning, and quota available events.
 * Tests ensure correct alert type, level, and message content generation
 * using JUnit 5 and Mockito framework.
 */
class QuotaAlertFactoryTest {
    private QuotaAlertFactory factory;
    @Mock
    private ClassSession mockClassSession;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes mock objects and configures the QuotaAlertFactory instance
     * with predefined mock ClassSession behavior including ID, professor,
     * maximum quota, and current quota for consistent testing across all test methods.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new QuotaAlertFactory();

        when(mockClassSession.getId()).thenReturn("CVDS-G01");
        when(mockClassSession.getProfessor()).thenReturn("Prof. García");
        when(mockClassSession.getMaxQuota()).thenReturn(30);
        when(mockClassSession.getCurrentQuota()).thenReturn(15);
    }

    /**
     * Tests the creation of QUOTA_FULL alert type.
     * Verifies that when a quota full event occurs, the factory creates
     * a critical alert with proper type, level, and message content
     * including class session ID and maximum quota information.
     */
    @Test
    void testCreateAlertQuotaFull() {
        Alert alert = factory.createAlert(mockClassSession, "QUOTA_FULL", null);

        assertNotNull(alert);
        assertEquals("QUOTA_FULL", alert.getType());
        assertEquals("CRITICAL", alert.getLevel());
        assertTrue(alert.getMessage().contains("CVDS-G01"));
        assertTrue(alert.getMessage().contains("30")); // maxQuota
        assertTrue(alert.getMessage().contains("cupo máximo"));
    }

    /**
     * Tests the creation of QUOTA_WARNING alert type.
     * Verifies that when a quota warning event occurs with percentage threshold,
     * the factory creates a warning alert with proper type, level, and message
     * content including class session ID, percentage threshold, and quota ratio.
     */
    @Test
    void testCreateAlertQuotaWarning() {
        Alert alert = factory.createAlert(mockClassSession, "QUOTA_90_PERCENT", 90);

        assertNotNull(alert);
        assertEquals("QUOTA_WARNING", alert.getType());
        assertEquals("WARNING", alert.getLevel());
        assertTrue(alert.getMessage().contains("CVDS-G01"));
        assertTrue(alert.getMessage().contains("90%"));
        assertTrue(alert.getMessage().contains("15/30"));
    }

    /**
     * Tests the creation of QUOTA_AVAILABLE alert type.
     * Verifies that when a quota available event occurs, the factory creates
     * an informational alert with proper type, level, and message content
     * including class session ID and availability notification message.
     */
    @Test
    void testCreateAlertQuotaAvailable() {
        Alert alert = factory.createAlert(mockClassSession, "QUOTA_AVAILABLE", null);

        assertNotNull(alert);
        assertEquals("QUOTA_AVAILABLE", alert.getType());
        assertEquals("INFO", alert.getLevel());
        assertTrue(alert.getMessage().contains("CVDS-G01"));
        assertTrue(alert.getMessage().contains("cupos disponibles"));
    }
}
