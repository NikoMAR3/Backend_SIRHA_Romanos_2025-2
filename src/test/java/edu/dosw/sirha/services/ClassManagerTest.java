package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ClassSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for testing the functionality of the ClassManager class.
 * This test class verifies proper class session retrieval, quota management,
 * and class session operations using reflection for accessing private fields.
 * Tests include class lookup, quota modification, quota checking, and internal
 * data structure manipulation using JUnit 5 and Mockito framework.
 */
class ClassManagerTest {
    private ClassManager classManager;
    private ClassSession mockClassSession;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes a ClassManager instance and creates a mock ClassSession
     * for use across multiple test methods, ensuring isolated test execution.
     */
    @BeforeEach
    void setUp() {
        classManager = new ClassManager();
        mockClassSession = mock(ClassSession.class);
    }

    /**
     * Tests the getClass method for class session retrieval.
     * Verifies that when a class session is stored in the manager with a
     * specific name, it can be retrieved correctly using case-insensitive
     * lookup, ensuring proper class session access functionality.
     */
    @Test
    void testGetClass() {
        String className = "math";
        classManager.getClassSessions().put(className.toLowerCase(), mockClassSession);
        ClassSession result = classManager.getClass(className);
        assertEquals(mockClassSession, result);
    }

    /**
     * Tests the modifyMaxClassQuota method for quota modification.
     * Verifies that when a class session exists in the manager, its
     * maximum quota can be properly modified through the manager's
     * interface, using Mockito to verify the setCurrentQuota method call.
     */
    @Test
    void testModifyMaxClassQuota() {
        String className = "math";
        int newQuota = 30;
        classManager.getClassSessions().put(className, mockClassSession);
        classManager.modifyMaxClassQuota(className, newQuota);
        verify(mockClassSession).setCurrentQuota(newQuota);
    }

    /**
     * Tests the checkMaxClassQuota method for quota retrieval.
     * Verifies that the manager can correctly retrieve the current quota
     * of a class session by class name, ensuring proper quota information
     * access through the manager's interface.
     */
    @Test
    void testCheckMaxClassQuota() {
        String className = "math";
        int quota = 25;
        when(mockClassSession.getCurrentQuota()).thenReturn(quota);
        classManager.getClassSessions().put(className, mockClassSession);
        int result = classManager.checkMaxClassQuota(className);
        assertEquals(quota, result);
    }

    /**
     * Tests the checkClassQuota method for quota checking by ClassSession object.
     * Verifies that the manager can retrieve quota information when provided
     * with a ClassSession object directly, ensuring alternative access
     * methods for quota information retrieval.
     */
    @Test
    void testCheckClassQuota() {
        String className = "math";
        int quota = 20;
        when(mockClassSession.getId()).thenReturn(className.toLowerCase());
        when(mockClassSession.getCurrentQuota()).thenReturn(quota);

        classManager.getClassSessions().put(className.toLowerCase(), mockClassSession);

        int result = classManager.checkClassQuota(mockClassSession);
        assertEquals(quota, result);
    }

    /**
     * Utility method for accessing the private classSessions field using reflection.
     * This method provides access to the internal HashMap storage of ClassManager
     * for testing purposes, allowing direct manipulation and verification of
     * the internal state without relying solely on public interface methods.
     *
     * @return HashMap containing the class sessions managed by the ClassManager
     * @throws RuntimeException if reflection access fails or field is not found
     */
    private HashMap<String, ClassSession> getClassSessions() {
        try {
            java.lang.reflect.Field field = ClassManager.class.getDeclaredField("classSessions");
            field.setAccessible(true);
            return (HashMap<String, ClassSession>) field.get(classManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
