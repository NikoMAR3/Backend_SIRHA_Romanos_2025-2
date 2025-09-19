package edu.dosw.sirha.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class SubjectTest {

    @Test
    void testDefaultConstructor() {
        Subject subject = new Subject();
        assertNotNull(subject);
        assertNotNull(subject.getPreRequisites());
    }

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

    @Test
    void testParameterizedConstructorWithNullPrerequisites() {
        Subject subject = new Subject("Programación", "CS101", null, 4);

        assertEquals("Programación", subject.getName());
        assertEquals("CS101", subject.getCode());
        assertEquals(4, subject.getCredits());
        assertNotNull(subject.getPreRequisites());
        assertTrue(subject.getPreRequisites().isEmpty());
    }

    @Test
    void testParameterizedConstructorWithEmptyPrerequisites() {
        Subject subject = new Subject("Base de Datos", "CS201", new ArrayList<>(), 3);

        assertEquals("Base de Datos", subject.getName());
        assertEquals("CS201", subject.getCode());
        assertEquals(3, subject.getCredits());
        assertTrue(subject.getPreRequisites().isEmpty());
    }

    @Test
    void testGetters() {
        Subject subject = new Subject("Algoritmos", "CS301", null, 4);
        assertEquals("Algoritmos", subject.getName());
        assertEquals("CS301", subject.getCode());
        assertEquals(4, subject.getCredits());
    }
}
