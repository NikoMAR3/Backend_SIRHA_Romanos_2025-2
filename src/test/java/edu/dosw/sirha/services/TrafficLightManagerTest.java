package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.model.TrafficLight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrafficLightManagerTest {
    private TrafficLightManager trafficLightManager;
    private Student mockStudent;
    private TrafficLight mockTrafficLight;

    @BeforeEach
    void setUp() {
        trafficLightManager = new TrafficLightManager();
        mockStudent = mock(Student.class);
        mockTrafficLight = mock(TrafficLight.class);
    }

    @Test
    void testGetTrafficLight() {
        getTrafficLights().put(mockStudent, mockTrafficLight);
        TrafficLight result = trafficLightManager.getTrafficLight(mockStudent);
        assertEquals(mockTrafficLight, result);
    }

    @Test
    void testCheckTrafficLight() {
        TrafficLight result = trafficLightManager.checkTrafficLight();

        assertNotNull(result);
        assertNull(result.getMajor());
        assertTrue(result.getSubjectsApproved().isEmpty());
        assertTrue(result.getSubjectsFailed().isEmpty());
        assertTrue(result.getAllSubjects().isEmpty());
    }

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