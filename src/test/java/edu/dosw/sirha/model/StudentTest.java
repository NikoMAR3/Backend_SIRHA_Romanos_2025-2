package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing the functionality of the Student class.
 * This test class verifies proper initialization, data management,
 * program enrollment, petition handling, and class session operations
 * of Student instances using JUnit 5 framework.
 */
class StudentTest {
    private Student student;
    private ClassSession classSession;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes a Student instance and a ClassSession instance
     * for use across multiple test methods.
     */
    @BeforeEach
    void setUp() {
        student = new Student("Juan Perez", "12345");
        classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 30);
    }

    /**
     * Tests the default constructor of Student.
     * Verifies that the instance is properly created and all collections
     * (programs, schedule, petitions) are initialized and not null.
     */
    @Test
    void testDefaultConstructor() {
        Student s = new Student();
        assertNotNull(s);
        assertNotNull(s.getPrograms());
        assertNotNull(s.getSchedule());
        assertNotNull(s.getPetitions());
    }

    /**
     * Tests the parameterized constructor of Student.
     * Verifies that all parameters are properly assigned and
     * all collections are initialized correctly.
     */
    @Test
    void testParameterizedConstructor() {
        assertEquals("Juan Perez", student.getFullName());
        assertEquals("12345", student.getId());
        assertNotNull(student.getPrograms());
        assertNotNull(student.getSchedule());
        assertNotNull(student.getPetitions());
    }

    /**
     * Tests the setter and getter methods for Student properties.
     * Verifies that name and ID can be properly modified and retrieved.
     */
    @Test
    void testSettersAndGetters() {
        student.setFullName("Maria Lopez");
        assertEquals("Maria Lopez", student.getFullName());

        student.setId("67890");
        assertEquals("67890", student.getId());
    }

    /**
     * Tests the addProgram functionality.
     * Verifies that programs can be added to the student's program list
     * and that the collection size is updated accordingly.
     */
    @Test
    void testAddProgram() {
        student.addProgram("Ingeniería de Sistemas");
        assertTrue(student.getPrograms().contains("Ingeniería de Sistemas"));

        student.addProgram("Matemáticas");
        assertEquals(2, student.getPrograms().size());
    }

    /**
     * Tests the addPetition functionality.
     * Verifies that petitions can be added to the student's petition list
     * and are properly contained in the collection.
     */
    @Test
    void testAddPetition() {
        Petition petition = new AddPetition("MAT101", "Solicitud de retiro", student, "G001");
        student.addPetition(petition);
        assertTrue(student.getPetitions().contains(petition));
    }

    /**
     * Tests null petition handling in addPetition method.
     * Verifies that adding a null petition does not affect
     * the size of the petitions collection.
     */
    @Test
    void testAddNullPetition() {
        int initialSize = student.getPetitions().size();
        student.addPetition(null);
        assertEquals(initialSize, student.getPetitions().size());
    }

    /**
     * Tests the addToClass functionality.
     * Verifies that a student can be added to a class session,
     * the class is added to the student's schedule, and the
     * class session's current quota is properly incremented.
     */
    @Test
    void testAddToClass() {
        student.addToClass(classSession);
        assertTrue(student.getSchedule().getClasses().contains(classSession));
        assertEquals(1, classSession.getCurrentQuota());
    }

    /**
     * Tests null handling in addToClass method.
     * Verifies that a NullPointerException is thrown when
     * attempting to add a student to a null class session.
     */
    @Test
    void testAddToClassNull() {
        assertThrows(NullPointerException.class, () -> student.addToClass(null));
    }

    /**
     * Tests the removeFromClass functionality.
     * Verifies that a student can be removed from a class session,
     * the class is removed from the student's schedule, and the
     * class session's current quota is properly decremented.
     */
    @Test
    void testRemoveFromClass() {
        student.addToClass(classSession);
        student.removeFromClass(classSession);
        assertFalse(student.getSchedule().getClasses().contains(classSession));
        assertEquals(0, classSession.getCurrentQuota());
    }
}
