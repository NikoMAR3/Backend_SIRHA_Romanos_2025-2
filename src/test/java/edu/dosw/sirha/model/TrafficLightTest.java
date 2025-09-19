package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrafficLightTest {
    private TrafficLight trafficLight;
    private Subject subject1;
    private Subject subject2;
    private Subject subject3;

    @BeforeEach
    void setUp() {
        trafficLight = new TrafficLight("Ingeniería de Sistemas");
        subject1 = new Subject("Matemáticas", "MAT101", null, 4);
        subject2 = new Subject("Programación", "CS101", null, 3);
        subject3 = new Subject("Base de Datos", "CS201", null, 3);
    }

    @Test
    void testConstructor() {
        assertEquals("Ingeniería de Sistemas", trafficLight.getMajor());
        assertNotNull(trafficLight.getSubjectsApproved());
        assertNotNull(trafficLight.getSubjectsFailed());
        assertNotNull(trafficLight.getAllSubjects());
        assertTrue(trafficLight.getSubjectsApproved().isEmpty());
        assertTrue(trafficLight.getSubjectsFailed().isEmpty());
        assertTrue(trafficLight.getAllSubjects().isEmpty());
    }

    @Test
    void testGetStatusVerde() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getSubjectsApproved().add(subject1);
        trafficLight.getSubjectsApproved().add(subject2);

        assertEquals("VERDE", trafficLight.getStatus());
    }

    @Test
    void testGetStatusAzul() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getAllSubjects().add(subject3);
        trafficLight.getSubjectsApproved().add(subject1);
        trafficLight.getSubjectsApproved().add(subject2);

        assertEquals("AZUL", trafficLight.getStatus());
    }

    @Test
    void testGetStatusRojo() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getAllSubjects().add(subject3);
        trafficLight.getSubjectsApproved().add(subject1);

        assertEquals("ROJO", trafficLight.getStatus());
    }

    @Test
    void testGetStatusWithNoSubjects() {
        assertEquals("ROJO", trafficLight.getStatus());
    }

    @Test
    void testGetStatusExactly80Percent() {
        Subject s1 = new Subject("S1", "S1", null, 8);
        Subject s2 = new Subject("S2", "S2", null, 2);

        trafficLight.getAllSubjects().add(s1);
        trafficLight.getAllSubjects().add(s2);
        trafficLight.getSubjectsApproved().add(s1);

        assertEquals("VERDE", trafficLight.getStatus());
    }

    @Test
    void testGetStatusExactly50Percent() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getSubjectsApproved().add(subject1);

        assertEquals("AZUL", trafficLight.getStatus());
    }

    @Test
    void testGetters() {
        assertEquals("Ingeniería de Sistemas", trafficLight.getMajor());
        assertTrue(trafficLight.getSubjectsApproved() instanceof java.util.ArrayList);
        assertTrue(trafficLight.getSubjectsFailed() instanceof java.util.ArrayList);
        assertTrue(trafficLight.getAllSubjects() instanceof java.util.ArrayList);
    }
}
