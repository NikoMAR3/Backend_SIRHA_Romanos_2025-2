package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class StudentTest {
    private Student student;
    private ClassSession classSession;

    @BeforeEach
    void setUp() {
        student = new Student("Juan Perez", "12345");
        classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 30);
    }

    @Test
    void testDefaultConstructor() {
        Student s = new Student();
        assertNotNull(s);
        assertNotNull(s.getPrograms());
        assertNotNull(s.getSchedule());
        assertNotNull(s.getPetitions());
    }

    @Test
    void testParameterizedConstructor() {
        assertEquals("Juan Perez", student.getName());
        assertEquals("12345", student.getId());
        assertNotNull(student.getPrograms());
        assertNotNull(student.getSchedule());
        assertNotNull(student.getPetitions());
    }

    @Test
    void testSettersAndGetters() {
        student.setName("Maria Lopez");
        assertEquals("Maria Lopez", student.getName());

        student.setId("67890");
        assertEquals("67890", student.getId());
    }

    @Test
    void testAddProgram() {
        student.addProgram("Ingeniería de Sistemas");
        assertTrue(student.getPrograms().contains("Ingeniería de Sistemas"));

        student.addProgram("Matemáticas");
        assertEquals(2, student.getPrograms().size());
    }

    @Test
    void testAddPetition() {
        Petition petition = new AddPetition("MAT101", "Solicitud de retiro", student, "G001");
        student.addPetition(petition);
        assertTrue(student.getPetitions().contains(petition));
    }

    @Test
    void testAddNullPetition() {
        int initialSize = student.getPetitions().size();
        student.addPetition(null);
        assertEquals(initialSize, student.getPetitions().size());
    }

    @Test
    void testAddToClass() {
        student.addToClass(classSession);
        assertTrue(student.getSchedule().getClasses().contains(classSession));
        assertEquals(1, classSession.getCurrentQuota());
    }

    @Test
    void testAddToClassNull() {
        assertThrows(NullPointerException.class, () -> student.addToClass(null));
    }

    @Test
    void testRemoveFromClass() {
        student.addToClass(classSession);
        student.removeFromClass(classSession);
        assertFalse(student.getSchedule().getClasses().contains(classSession));
        assertEquals(0, classSession.getCurrentQuota());
    }
}