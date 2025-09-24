package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.model.TrafficLight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for testing the functionality of the TrafficLightManager class.
 * This test class verifies proper traffic light management operations including
 * traffic light retrieval, creation, and storage. Tests utilize reflection for
 * accessing private fields and Mockito framework for mock object creation
 * using JUnit 5 framework.
 */
class TrafficLightManagerTest {
    private TrafficLightManager trafficLightManager;
    private Student mockStudent;
    private TrafficLight mockTrafficLight;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes a TrafficLightManager instance and creates mock objects for
     * Student and TrafficLight for use across multiple test methods,
     * ensuring isolated test execution and consistent mock behavior.
     */
    @BeforeEach
    void setUp() {
        trafficLightManager = new TrafficLightManager();
        mockStudent = mock(Student.class);
        mockTrafficLight = mock(TrafficLight.class);
    }

    /**
     * Tests the getTrafficLight method for traffic light retrieval.
     * Verifies that when a traffic light is stored for a specific student,
     * it can be correctly retrieved using the student as the key,
     * ensuring proper traffic light lookup functionality.
     */
    @Test
    void testGetTrafficLight() {
        getTrafficLights().put(mockStudent, mockTrafficLight);
        TrafficLight result = trafficLightManager.getTrafficLight(mockStudent);
        assertEquals(mockTrafficLight, result);
    }

    /**
     * Tests the checkTrafficLight method for traffic light creation.
     * Verifies that the method returns a new TrafficLight instance with
     * proper initialization - null major and empty collections for
     * approved subjects, failed subjects, and all subjects.
     */
    @Test
    void testCheckTrafficLight() {
        TrafficLight result = trafficLightManager.checkTrafficLight();

        assertNotNull(result);
        assertNull(result.getMajor());
        assertTrue(result.getSubjectsApproved().isEmpty());
        assertTrue(result.getSubjectsFailed().isEmpty());
        assertTrue(result.getAllSubjects().isEmpty());
    }

    /**
     * Utility method for accessing the private trafficLights field using reflection.
     * This method provides access to the internal HashMap storage of TrafficLightManager
     * for testing purposes, allowing direct manipulation and verification of
     * the internal state without relying solely on public interface methods.
     *
     * @return HashMap containing the student-traffic light mappings managed by the TrafficLightManager
     * @throws RuntimeException if reflection access fails or field is not found
     */
    private HashMap<Student, TrafficLight> getTrafficLights() {
        try {
            java.lang.reflect.Field field = TrafficLightManager.class.getDeclaredField("trafficLights");
            field.setAccessible(true);
            return (HashMap<Student, TrafficLight>) field.get(trafficLightManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
