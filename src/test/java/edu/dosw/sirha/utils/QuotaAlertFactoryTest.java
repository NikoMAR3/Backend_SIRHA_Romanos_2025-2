package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.services.observer.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class QuotaAlertFactoryTest {
    private QuotaAlertFactory factory;
    @Mock
    private ClassSession mockClassSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new QuotaAlertFactory();

        when(mockClassSession.getId()).thenReturn("CVDS-G01");
        when(mockClassSession.getProfessor()).thenReturn("Prof. García");
        when(mockClassSession.getMaxQuota()).thenReturn(30);
        when(mockClassSession.getCurrentQuota()).thenReturn(15);
    }

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
