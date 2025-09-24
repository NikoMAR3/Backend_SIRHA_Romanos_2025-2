package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing the functionality of the TrafficLight class.
 * This test class verifies proper initialization, status calculation,
 * subject management, and academic progress tracking based on credit
 * percentage thresholds using JUnit 5 framework.
 */
class TrafficLightTest {
    private TrafficLight trafficLight;
    private Subject subject1;
    private Subject subject2;
    private Subject subject3;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes a TrafficLight instance for "Ingeniería de Sistemas" major
     * and creates three Subject instances with different credits for testing
     * various status calculation scenarios.
     */
    @BeforeEach
    void setUp() {
        trafficLight = new TrafficLight("Ingeniería de Sistemas");
        subject1 = new Subject("Matemáticas", "MAT101", null, 4);
        subject2 = new Subject("Programación", "CS101", null, 3);
        subject3 = new Subject("Base de Datos", "CS201", null, 3);
    }

    /**
     * Tests the TrafficLight constructor.
     * Verifies that the instance is properly initialized with the correct major,
     * all subject collections are not null, and all collections start empty.
     */
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

    /**
     * Tests the GREEN status calculation.
     * Verifies that when 100% of subjects (by credits) are approved,
     * the traffic light status returns "VERDE" indicating excellent progress.
     */
    @Test
    void testGetStatusVerde() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getSubjectsApproved().add(subject1);
        trafficLight.getSubjectsApproved().add(subject2);

        assertEquals("VERDE", trafficLight.getStatus());
    }

    /**
     * Tests the BLUE status calculation.
     * Verifies that when the percentage of approved credits falls between
     * 50% and 80%, the traffic light status returns "AZUL" indicating
     * moderate progress.
     */
    @Test
    void testGetStatusAzul() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getAllSubjects().add(subject3);
        trafficLight.getSubjectsApproved().add(subject1);
        trafficLight.getSubjectsApproved().add(subject2);

        assertEquals("AZUL", trafficLight.getStatus());
    }

    /**
     * Tests the RED status calculation.
     * Verifies that when the percentage of approved credits is below 50%,
     * the traffic light status returns "ROJO" indicating poor progress
     * and potential academic risk.
     */
    @Test
    void testGetStatusRojo() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getAllSubjects().add(subject3);
        trafficLight.getSubjectsApproved().add(subject1);

        assertEquals("ROJO", trafficLight.getStatus());
    }

    /**
     * Tests the status calculation with no subjects enrolled.
     * Verifies that when no subjects are registered in the traffic light,
     * the default status is "ROJO" indicating critical academic status.
     */
    @Test
    void testGetStatusWithNoSubjects() {
        assertEquals("ROJO", trafficLight.getStatus());
    }

    /**
     * Tests the status calculation at exactly 80% threshold.
     * Verifies that when exactly 80% of credits are approved,
     * the status is "VERDE", confirming the boundary condition
     * for the green status threshold.
     */
    @Test
    void testGetStatusExactly80Percent() {
        Subject s1 = new Subject("S1", "S1", null, 8);
        Subject s2 = new Subject("S2", "S2", null, 2);

        trafficLight.getAllSubjects().add(s1);
        trafficLight.getAllSubjects().add(s2);
        trafficLight.getSubjectsApproved().add(s1);

        assertEquals("VERDE", trafficLight.getStatus());
    }

    /**
     * Tests the status calculation at exactly 50% threshold.
     * Verifies that when exactly 50% of credits are approved,
     * the status is "AZUL", confirming the boundary condition
     * between blue and red status thresholds.
     */
    @Test
    void testGetStatusExactly50Percent() {
        trafficLight.getAllSubjects().add(subject1);
        trafficLight.getAllSubjects().add(subject2);
        trafficLight.getSubjectsApproved().add(subject1);

        assertEquals("AZUL", trafficLight.getStatus());
    }

    /**
     * Tests all getter methods and collection types.
     * Verifies that the major is correctly returned and that all
     * subject collections are implemented as ArrayList instances,
     * ensuring consistent collection behavior.
     */
    @Test
    void testGetters() {
        assertEquals("Ingeniería de Sistemas", trafficLight.getMajor());
        assertTrue(trafficLight.getSubjectsApproved() instanceof java.util.ArrayList);
        assertTrue(trafficLight.getSubjectsFailed() instanceof java.util.ArrayList);
        assertTrue(trafficLight.getAllSubjects() instanceof java.util.ArrayList);
    }
}
