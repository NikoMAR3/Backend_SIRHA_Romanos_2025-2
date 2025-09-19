package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.model.TrafficLight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        trafficLightManager.getTrafficLights().put(mockStudent, mockTrafficLight);
        TrafficLight result = trafficLightManager.getTrafficLight(mockStudent);
        assertEquals(mockTrafficLight, result);
    }

    @Test
    void testCheckTrafficLight() {
        TrafficLight defaultTrafficLight = mock(TrafficLight.class);
        when(TrafficLight.getDefaultTrafficLight()).thenReturn(defaultTrafficLight);
        TrafficLight result = trafficLightManager.checkTrafficLight();
        assertEquals(defaultTrafficLight, result);
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