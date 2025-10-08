package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ObserverService.
 * Validates monitoring functionality for class session capacity management,
 * alert generation, and capacity threshold detection.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ObserverService Tests")
class ObserverServiceTest {

    @Mock
    private ClassSessionRepository classSessionRepository;

    @InjectMocks
    private ObserverService observerService;

    private ClassSession testSession;
    private ClassSession warningSession;
    private ClassSession fullSession;
    private ClassSession emptySession;
    private ClassSession invalidCapacitySession;

    private Logger logger;
    private ListAppender<ILoggingEvent> logAppender;

    /**
     * Sets up test data and logging capture before each test execution.
     * Creates sample class sessions with different capacity scenarios.
     */
    @BeforeEach
    void setUp() {
        
        logger = (Logger) LoggerFactory.getLogger(ObserverService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        
        testSession = new ClassSession();
        testSession.setId("session123");
        testSession.setSubjectName("Programación Orientada a Objetos");
        testSession.setSubjectShortName("POO");
        testSession.setCapacity(30);
        testSession.setEnrolledStudents(15);
        testSession.setStartDate(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0));
        testSession.setEndDate(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));

        
        warningSession = new ClassSession();
        warningSession.setId("warning123");
        warningSession.setSubjectName("Estructuras de Datos");
        warningSession.setSubjectShortName("ED");
        warningSession.setCapacity(20);
        warningSession.setEnrolledStudents(18); // 90%
        warningSession.setStartDate(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0));
        warningSession.setEndDate(LocalDateTime.now().plusDays(2).withHour(12).withMinute(0).withSecond(0).withNano(0));

     
        fullSession = new ClassSession();
        fullSession.setId("full123");
        fullSession.setSubjectName("Base de Datos");
        fullSession.setSubjectShortName("BD");
        fullSession.setCapacity(25);
        fullSession.setEnrolledStudents(25); // 100%
        fullSession.setStartDate(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
        fullSession.setEndDate(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0));

       
        emptySession = new ClassSession();
        emptySession.setId("empty123");
        emptySession.setSubjectName("Matemáticas Discretas");
        emptySession.setSubjectShortName("MD");
        emptySession.setCapacity(40);
        emptySession.setEnrolledStudents(0);
        emptySession.setStartDate(LocalDateTime.now().plusDays(4).withHour(16).withMinute(0).withSecond(0).withNano(0));
        emptySession.setEndDate(LocalDateTime.now().plusDays(4).withHour(18).withMinute(0).withSecond(0).withNano(0));

        invalidCapacitySession = new ClassSession();
        invalidCapacitySession.setId("invalid123");
        invalidCapacitySession.setSubjectName("Sesión Inválida");
        invalidCapacitySession.setSubjectShortName("INV");
        invalidCapacitySession.setCapacity(0);
        invalidCapacitySession.setEnrolledStudents(5);
        invalidCapacitySession.setStartDate(LocalDateTime.now().plusDays(5).withHour(18).withMinute(0).withSecond(0).withNano(0));
        invalidCapacitySession.setEndDate(LocalDateTime.now().plusDays(5).withHour(20).withMinute(0).withSecond(0).withNano(0));
    }

    @Nested
    @DisplayName("monitorLoadClassSession() Tests")
    class MonitorLoadClassSessionTests {

        /**
         * Tests successful monitoring of a normal capacity session.
         */
        @Test
        @DisplayName("Should monitor session successfully when capacity is normal")
        void monitorLoadClassSession_NormalCapacity_ShouldNotGenerateAlerts() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            assertDoesNotThrow(() -> observerService.monitorLoadClassSession("session123"));

            verify(classSessionRepository).findById("session123");
            
            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().noneMatch(event -> 
                event.getMessage().contains("WARNING") || event.getMessage().contains("CRITICAL")));
        }

        /**
         * Tests warning alert generation for 90% capacity.
        
        @Test
        @DisplayName("Should generate warning alert when session reaches 90% capacity")
        void monitorLoadClassSession_WarningCapacity_ShouldGenerateWarning() {
            when(classSessionRepository.findById("warning123")).thenReturn(Optional.of(warningSession));

            observerService.monitorLoadClassSession("warning123");

            verify(classSessionRepository).findById("warning123");
            
            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("CAPACITY WARNING") && 
                event.getMessage().contains("warning123")));
        }
        */

        /**
         Tests critical alert generation for full capacity.
        
        @Test
        @DisplayName("Should generate critical alert when session is full")
        void monitorLoadClassSession_FullCapacity_ShouldGenerateCriticalAlert() {
            when(classSessionRepository.findById("full123")).thenReturn(Optional.of(fullSession));

            observerService.monitorLoadClassSession("full123");

            verify(classSessionRepository).findById("full123");
            
            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("FULL CAPACITY ALERT") && 
                event.getMessage().contains("full123")));
        }
        */

        /**
         * Tests monitoring of empty session.
         */
        @Test
        @DisplayName("Should monitor empty session without generating alerts")
        void monitorLoadClassSession_EmptySession_ShouldNotGenerateAlerts() {
            when(classSessionRepository.findById("empty123")).thenReturn(Optional.of(emptySession));

            observerService.monitorLoadClassSession("empty123");

            verify(classSessionRepository).findById("empty123");
            
           
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().noneMatch(event -> 
                event.getMessage().contains("WARNING") || event.getMessage().contains("CRITICAL")));
        }

        /**
         * Tests monitoring session with invalid capacity.
         */
        @Test
        @DisplayName("Should handle invalid capacity gracefully with warning log")
        void monitorLoadClassSession_InvalidCapacity_ShouldLogWarning() {
            when(classSessionRepository.findById("invalid123")).thenReturn(Optional.of(invalidCapacitySession));

            observerService.monitorLoadClassSession("invalid123");

            verify(classSessionRepository).findById("invalid123");
            
            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("has invalid capacity")));
        }

        /**
         * Tests exception when session ID is null.
         */
        @Test
        @DisplayName("Should throw exception when session ID is null")
        void monitorLoadClassSession_NullSessionId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.monitorLoadClassSession(null)
            );

            assertEquals("Session ID cannot be null or empty", exception.getMessage());
            verify(classSessionRepository, never()).findById(anyString());
        }

        /**
         * Tests exception when session ID is empty.
         */
        @Test
        @DisplayName("Should throw exception when session ID is empty")
        void monitorLoadClassSession_EmptySessionId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.monitorLoadClassSession("   ")
            );

            assertEquals("Session ID cannot be null or empty", exception.getMessage());
            verify(classSessionRepository, never()).findById(anyString());
        }

        /**
         * Tests exception when session is not found.
         */
        @Test
        @DisplayName("Should throw exception when session is not found")
        void monitorLoadClassSession_SessionNotFound_ShouldThrowException() {
            when(classSessionRepository.findById("nonexistent123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.monitorLoadClassSession("nonexistent123")
            );

            assertEquals("Class session with ID 'nonexistent123' not found", exception.getMessage());
            verify(classSessionRepository).findById("nonexistent123");
        }

        /**
         * Tests monitoring session with over capacity (more than 100%).
         */
        @Test
        @DisplayName("Should generate critical alert when session is over capacity")
        void monitorLoadClassSession_OverCapacity_ShouldGenerateCriticalAlert() {
            ClassSession overCapacitySession = new ClassSession();
            overCapacitySession.setId("over123");
            overCapacitySession.setSubjectName("Sesión Sobrecargada");
            overCapacitySession.setSubjectShortName("SO");
            overCapacitySession.setCapacity(20);
            overCapacitySession.setEnrolledStudents(25); 

            when(classSessionRepository.findById("over123")).thenReturn(Optional.of(overCapacitySession));

            observerService.monitorLoadClassSession("over123");

            // Verificar que se generó una alerta crítica
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("FULL CAPACITY ALERT")));
        }

        /**
         * Tests monitoring session exactly at warning threshold.
         */
        @Test
        @DisplayName("Should generate warning alert when session is exactly at 90% capacity")
        void monitorLoadClassSession_ExactlyAtThreshold_ShouldGenerateWarning() {
            ClassSession exactThresholdSession = new ClassSession();
            exactThresholdSession.setId("exact123");
            exactThresholdSession.setSubjectName("Sesión Exacta");
            exactThresholdSession.setSubjectShortName("EX");
            exactThresholdSession.setCapacity(10);
            exactThresholdSession.setEnrolledStudents(9); 

            when(classSessionRepository.findById("exact123")).thenReturn(Optional.of(exactThresholdSession));

            observerService.monitorLoadClassSession("exact123");

            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("CAPACITY WARNING")));
        }
    }

    @Nested
    @DisplayName("monitorAllClassSessions() Tests")
    class MonitorAllClassSessionsTests {

        /**
         * Tests monitoring all sessions with mixed capacity levels.
        
        @Test
        @DisplayName("Should monitor all sessions and generate appropriate alerts")
        void monitorAllClassSessions_MixedCapacities_ShouldGenerateAppropriateAlerts() {
            List<ClassSession> allSessions = Arrays.asList(testSession, warningSession, fullSession, emptySession);
            when(classSessionRepository.findAll()).thenReturn(allSessions);

            observerService.monitorAllClassSessions();

            verify(classSessionRepository).findAll();
            
            List<ILoggingEvent> logEvents = logAppender.list;
            
            
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("CAPACITY WARNING")));
            
          
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("FULL CAPACITY ALERT")));
            
            
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("Monitoring completed for 4 class sessions")));
        }
        */

        /**
         * Tests monitoring when no sessions exist.
         */
        @Test
        @DisplayName("Should handle empty session list gracefully")
        void monitorAllClassSessions_NoSessions_ShouldLogInfo() {
            when(classSessionRepository.findAll()).thenReturn(new ArrayList<>());

            observerService.monitorAllClassSessions();

            verify(classSessionRepository).findAll();
            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("No class sessions found to monitor")));
        }

        /**
         * Tests monitoring sessions with invalid capacities.
        
        @Test
        @DisplayName("Should skip sessions with invalid capacity during monitoring")
        void monitorAllClassSessions_WithInvalidCapacities_ShouldSkipInvalidSessions() {
            List<ClassSession> allSessions = Arrays.asList(testSession, invalidCapacitySession, warningSession);
            when(classSessionRepository.findAll()).thenReturn(allSessions);

            observerService.monitorAllClassSessions();

            verify(classSessionRepository).findAll();
            
            List<ILoggingEvent> logEvents = logAppender.list;
            
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("CAPACITY WARNING")));
            
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("Monitoring completed for 3 class sessions")));
        }
        */

        /**
         * Tests monitoring with single session.
        
        @Test
        @DisplayName("Should monitor single session correctly")
        void monitorAllClassSessions_SingleSession_ShouldMonitorCorrectly() {
            List<ClassSession> singleSession = Arrays.asList(fullSession);
            when(classSessionRepository.findAll()).thenReturn(singleSession);

            observerService.monitorAllClassSessions();

            verify(classSessionRepository).findAll();
            
            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("FULL CAPACITY ALERT")));
            
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("Monitoring completed for 1 class sessions")));
        }
        */
    }

    @Nested
    @DisplayName("isSessionAtWarningCapacity() Tests")
    class IsSessionAtWarningCapacityTests {

        /**
         * Tests warning capacity detection for session at 90%.
         */
        @Test
        @DisplayName("Should return true when session is at warning capacity")
        void isSessionAtWarningCapacity_WarningLevel_ShouldReturnTrue() {
            when(classSessionRepository.findById("warning123")).thenReturn(Optional.of(warningSession));

            boolean result = observerService.isSessionAtWarningCapacity("warning123");

            assertTrue(result);
            verify(classSessionRepository).findById("warning123");
        }

        /**
         * Tests warning capacity detection for normal session.
         */
        @Test
        @DisplayName("Should return false when session is at normal capacity")
        void isSessionAtWarningCapacity_NormalLevel_ShouldReturnFalse() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            boolean result = observerService.isSessionAtWarningCapacity("session123");

            assertFalse(result);
            verify(classSessionRepository).findById("session123");
        }

        /**
         * Tests warning capacity detection for full session.
         */
        @Test
        @DisplayName("Should return true when session is full")
        void isSessionAtWarningCapacity_FullCapacity_ShouldReturnTrue() {
            when(classSessionRepository.findById("full123")).thenReturn(Optional.of(fullSession));

            boolean result = observerService.isSessionAtWarningCapacity("full123");

            assertTrue(result);
            verify(classSessionRepository).findById("full123");
        }

        /**
         * Tests warning capacity detection for empty session.
         */
        @Test
        @DisplayName("Should return false when session is empty")
        void isSessionAtWarningCapacity_EmptySession_ShouldReturnFalse() {
            when(classSessionRepository.findById("empty123")).thenReturn(Optional.of(emptySession));

            boolean result = observerService.isSessionAtWarningCapacity("empty123");

            assertFalse(result);
            verify(classSessionRepository).findById("empty123");
        }

        /**
         * Tests warning capacity detection for invalid capacity.
         */
        @Test
        @DisplayName("Should return false when session has invalid capacity")
        void isSessionAtWarningCapacity_InvalidCapacity_ShouldReturnFalse() {
            when(classSessionRepository.findById("invalid123")).thenReturn(Optional.of(invalidCapacitySession));

            boolean result = observerService.isSessionAtWarningCapacity("invalid123");

            assertFalse(result);
            verify(classSessionRepository).findById("invalid123");
        }

        /**
         * Tests exception when session ID is null.
         */
        @Test
        @DisplayName("Should throw exception when session ID is null")
        void isSessionAtWarningCapacity_NullSessionId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.isSessionAtWarningCapacity(null)
            );

            assertEquals("Session ID cannot be null or empty", exception.getMessage());
        }

        /**
         * Tests exception when session is not found.
         */
        @Test
        @DisplayName("Should throw exception when session is not found")
        void isSessionAtWarningCapacity_SessionNotFound_ShouldThrowException() {
            when(classSessionRepository.findById("nonexistent123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.isSessionAtWarningCapacity("nonexistent123")
            );

            assertEquals("Class session with ID 'nonexistent123' not found", exception.getMessage());
        }

        /**
         * Tests exact threshold boundary.
         */
        @Test
        @DisplayName("Should return true when session is exactly at 90% capacity")
        void isSessionAtWarningCapacity_ExactThreshold_ShouldReturnTrue() {
            ClassSession exactThresholdSession = new ClassSession();
            exactThresholdSession.setCapacity(10);
            exactThresholdSession.setEnrolledStudents(9); 

            when(classSessionRepository.findById("exact123")).thenReturn(Optional.of(exactThresholdSession));

            boolean result = observerService.isSessionAtWarningCapacity("exact123");

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("isSessionFull() Tests")
    class IsSessionFullTests {

        /**
         * Tests full capacity detection for completely full session.
         */
        @Test
        @DisplayName("Should return true when session is completely full")
        void isSessionFull_FullCapacity_ShouldReturnTrue() {
            when(classSessionRepository.findById("full123")).thenReturn(Optional.of(fullSession));

            boolean result = observerService.isSessionFull("full123");

            assertTrue(result);
            verify(classSessionRepository).findById("full123");
        }

        /**
         * Tests full capacity detection for normal session.
         */
        @Test
        @DisplayName("Should return false when session is not full")
        void isSessionFull_NormalCapacity_ShouldReturnFalse() {
            when(classSessionRepository.findById("session123")).thenReturn(Optional.of(testSession));

            boolean result = observerService.isSessionFull("session123");

            assertFalse(result);
            verify(classSessionRepository).findById("session123");
        }

        /**
         * Tests full capacity detection for over-capacity session.
         */
        @Test
        @DisplayName("Should return true when session is over capacity")
        void isSessionFull_OverCapacity_ShouldReturnTrue() {
            ClassSession overCapacitySession = new ClassSession();
            overCapacitySession.setCapacity(20);
            overCapacitySession.setEnrolledStudents(25); 

            when(classSessionRepository.findById("over123")).thenReturn(Optional.of(overCapacitySession));

            boolean result = observerService.isSessionFull("over123");

            assertTrue(result);
        }

        /**
         * Tests full capacity detection for empty session.
         */
        @Test
        @DisplayName("Should return false when session is empty")
        void isSessionFull_EmptySession_ShouldReturnFalse() {
            when(classSessionRepository.findById("empty123")).thenReturn(Optional.of(emptySession));

            boolean result = observerService.isSessionFull("empty123");

            assertFalse(result);
            verify(classSessionRepository).findById("empty123");
        }

        /**
         * Tests exception when session ID is null.
         */
        @Test
        @DisplayName("Should throw exception when session ID is null")
        void isSessionFull_NullSessionId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.isSessionFull(null)
            );

            assertEquals("Session ID cannot be null or empty", exception.getMessage());
        }

        /**
         * Tests exception when session is not found.
         */
        @Test
        @DisplayName("Should throw exception when session is not found")
        void isSessionFull_SessionNotFound_ShouldThrowException() {
            when(classSessionRepository.findById("nonexistent123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> observerService.isSessionFull("nonexistent123")
            );

            assertEquals("Class session with ID 'nonexistent123' not found", exception.getMessage());
        }

        /**
         * Tests exact capacity boundary.
         */
        @Test
        @DisplayName("Should return true when enrolled students equals capacity")
        void isSessionFull_ExactCapacity_ShouldReturnTrue() {
            ClassSession exactCapacitySession = new ClassSession();
            exactCapacitySession.setCapacity(15);
            exactCapacitySession.setEnrolledStudents(15); 

            when(classSessionRepository.findById("exact123")).thenReturn(Optional.of(exactCapacitySession));

            boolean result = observerService.isSessionFull("exact123");

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Alert Generation Tests")
    class AlertGenerationTests {

        /**
         * Tests warning alert message format.
        
        @Test
        @DisplayName("Should generate properly formatted warning alert")
        void monitorLoadClassSession_ShouldGenerateProperlyFormattedWarningAlert() {
            when(classSessionRepository.findById("warning123")).thenReturn(Optional.of(warningSession));

            observerService.monitorLoadClassSession("warning123");

            List<ILoggingEvent> logEvents = logAppender.list;
            Optional<ILoggingEvent> warningEvent = logEvents.stream()
                .filter(event -> event.getMessage().contains("CAPACITY WARNING"))
                .findFirst();

            assertTrue(warningEvent.isPresent());
            String message = warningEvent.get().getFormattedMessage();
            assertTrue(message.contains("ED"));
            assertTrue(message.contains("Estructuras de Datos"));
            assertTrue(message.contains("90.0%"));
            assertTrue(message.contains("18/20"));
        }
        */

        /**
         * Tests critical alert message format.
         */
        @Test
        @DisplayName("Should generate properly formatted critical alert")
        void monitorLoadClassSession_ShouldGenerateProperlyFormattedCriticalAlert() {
            when(classSessionRepository.findById("full123")).thenReturn(Optional.of(fullSession));

            observerService.monitorLoadClassSession("full123");

            List<ILoggingEvent> logEvents = logAppender.list;
            Optional<ILoggingEvent> criticalEvent = logEvents.stream()
                .filter(event -> event.getMessage().contains("FULL CAPACITY ALERT"))
                .findFirst();

            assertTrue(criticalEvent.isPresent());
            String message = criticalEvent.get().getFormattedMessage();
            assertTrue(message.contains("BD"));
            assertTrue(message.contains("Base de Datos"));
            assertTrue(message.contains("25/25"));
            assertTrue(message.contains("FULL"));
        }

        /**
         * Tests alert generation for different percentages.
        
        @Test
        @DisplayName("Should generate alerts for various capacity percentages")
        void monitorLoadClassSession_VariousPercentages_ShouldGenerateCorrectAlerts() {
            // Sesión al 95%
            ClassSession highCapacitySession = new ClassSession();
            highCapacitySession.setId("high123");
            highCapacitySession.setSubjectName("Algoritmos");
            highCapacitySession.setSubjectShortName("ALG");
            highCapacitySession.setCapacity(20);
            highCapacitySession.setEnrolledStudents(19); 

            when(classSessionRepository.findById("high123")).thenReturn(Optional.of(highCapacitySession));

            observerService.monitorLoadClassSession("high123");

            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getFormattedMessage().contains("95.0%")));
        }
        */
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        /**
         * Tests monitoring with zero capacity and zero students.
         */
        @Test
        @DisplayName("Should handle zero capacity and zero students")
        void monitorLoadClassSession_ZeroCapacityZeroStudents_ShouldLogWarning() {
            ClassSession zeroSession = new ClassSession();
            zeroSession.setId("zero123");
            zeroSession.setCapacity(0);
            zeroSession.setEnrolledStudents(0);

            when(classSessionRepository.findById("zero123")).thenReturn(Optional.of(zeroSession));

            observerService.monitorLoadClassSession("zero123");

            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("has invalid capacity")));
        }

        /**
         * Tests monitoring with negative capacity.
        
        @Test
        @DisplayName("Should handle negative capacity gracefully")
        void monitorLoadClassSession_NegativeCapacity_ShouldLogWarning() {
            ClassSession negativeSession = new ClassSession();
            negativeSession.setId("negative123");
            negativeSession.setCapacity(-5);
            negativeSession.setEnrolledStudents(3);

            when(classSessionRepository.findById("negative123")).thenReturn(Optional.of(negativeSession));

            observerService.monitorLoadClassSession("negative123");

            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getMessage().contains("has invalid capacity") && 
                event.getMessage().contains("-5")));
        }
        */

        /**
         * Tests large capacity numbers.
         */
        @Test
        @DisplayName("Should handle large capacity numbers correctly")
        void monitorLoadClassSession_LargeNumbers_ShouldHandleCorrectly() {
            ClassSession largeSession = new ClassSession();
            largeSession.setId("large123");
            largeSession.setSubjectName("Conferencia Masiva");
            largeSession.setSubjectShortName("CM");
            largeSession.setCapacity(1000);
            largeSession.setEnrolledStudents(900); 

            when(classSessionRepository.findById("large123")).thenReturn(Optional.of(largeSession));

            observerService.monitorLoadClassSession("large123");

            List<ILoggingEvent> logEvents = logAppender.list;
            assertTrue(logEvents.stream().anyMatch(event -> 
                event.getFormattedMessage().contains("900/1000")));
        }

        /**
         * Tests session ID with special characters.
         */
        @Test
        @DisplayName("Should handle session IDs with special characters")
        void monitorLoadClassSession_SpecialCharacters_ShouldHandleCorrectly() {
            String specialId = "session-123_test@domain.com";
            when(classSessionRepository.findById(specialId)).thenReturn(Optional.of(testSession));

            assertDoesNotThrow(() -> observerService.monitorLoadClassSession(specialId));

            verify(classSessionRepository).findById(specialId);
        }

        /**
         * Tests combination of monitoring methods.
         */
        @Test
        @DisplayName("Should work correctly when combining different monitoring methods")
        void combinedMonitoring_ShouldWorkConsistently() {
            when(classSessionRepository.findById("warning123")).thenReturn(Optional.of(warningSession));

            observerService.monitorLoadClassSession("warning123");
            boolean isAtWarning = observerService.isSessionAtWarningCapacity("warning123");
            boolean isFull = observerService.isSessionFull("warning123");

            assertTrue(isAtWarning);
            assertFalse(isFull);

            verify(classSessionRepository, times(3)).findById("warning123");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        /**
         * Tests that the service can be instantiated with repository dependency.
         */
        @Test
        @DisplayName("Should create service with repository dependency")
        void constructor_ShouldCreateServiceWithRepository() {
            ClassSessionRepository repository = mock(ClassSessionRepository.class);

            ObserverService service = new ObserverService(repository);

            assertNotNull(service);
        }
    }
}