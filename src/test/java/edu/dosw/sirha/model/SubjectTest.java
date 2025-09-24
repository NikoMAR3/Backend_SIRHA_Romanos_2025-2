package edu.dosw.sirha.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing the functionality of the Subject class.
 * This test class verifies proper initialization, constructor behavior,
 * prerequisite management, and data retrieval operations of Subject instances
 */
class SubjectTest {

    /**
     * Tests the default constructor of Subject.
     * Verifies that the instance is properly created and
     * the prerequisites collection is initialized and not null.
     */
    @Test
    void testDefaultConstructor() {
        Subject subject = new Subject();
        assertNotNull(subject);
        assertNotNull(subject.getPreRequisites());
    }

    /**
     * Tests the parameterized constructor with valid prerequisites.
     * Verifies that all parameters are properly assigned, including
     * name, code, credits, and that prerequisites are correctly stored
     * and accessible through the collection.
     */
    @Test
    void testParameterizedConstructorWithPrerequisites() {
        Subject prereq1 = new Subject("Matemáticas I", "MAT101", null, 4);
        Subject prereq2 = new Subject("Física I", "FIS101", null, 3);

        Subject subject = new Subject("Cálculo", "MAT201", Arrays.asList(prereq1, prereq2), 5);

        assertEquals("Cálculo", subject.getName());
        assertEquals("MAT201", subject.getCode());
        assertEquals(5, subject.getCredits());
        assertEquals(2, subject.getPreRequisites().size());
        assertTrue(subject.getPreRequisites().contains(prereq1));
    }

    /**
     * Tests the parameterized constructor with null prerequisites.
     * Verifies that when null is passed as prerequisites parameter,
     * the prerequisites collection is properly initialized as empty
     * but not null, ensuring safe access to the collection.
     */
    @Test
    void testParameterizedConstructorWithNullPrerequisites() {
        Subject subject = new Subject("Programación", "CS101", null, 4);

        assertEquals("Programación", subject.getName());
        assertEquals("CS101", subject.getCode());
        assertEquals(4, subject.getCredits());
        assertNotNull(subject.getPreRequisites());
        assertTrue(subject.getPreRequisites().isEmpty());
    }

    /**
     * Tests the parameterized constructor with empty prerequisites list.
     * Verifies that when an empty ArrayList is passed as prerequisites,
     * all other parameters are correctly assigned and the prerequisites
     * collection remains empty but accessible.
     */
    @Test
    void testParameterizedConstructorWithEmptyPrerequisites() {
        Subject subject = new Subject("Base de Datos", "CS201", new ArrayList<>(), 3);

        assertEquals("Base de Datos", subject.getName());
        assertEquals("CS201", subject.getCode());
        assertEquals(3, subject.getCredits());
        assertTrue(subject.getPreRequisites().isEmpty());
    }

    /**
     * Tests all getter methods of the Subject class.
     * Verifies that all properties (name, code, credits) are properly
     * accessible through their respective getter methods after
     * initialization through the parameterized constructor.
     */
    @Test
    void testGetters() {
        Subject subject = new Subject("Algoritmos", "CS301", null, 4);
        assertEquals("Algoritmos", subject.getName());
        assertEquals("CS301", subject.getCode());
        assertEquals(4, subject.getCredits());
    }
}
