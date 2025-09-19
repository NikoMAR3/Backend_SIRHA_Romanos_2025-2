package edu.dosw.sirha.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ClassSessionTest {
    private ClassSession classSession;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 2);
        student1 = new Student("Juan", "12345");
        student2 = new Student("Maria", "67890");
    }

    @Test
    void testDefaultConstructor() {
        ClassSession cs = new ClassSession();
        assertNotNull(cs);
        assertEquals(0, cs.getCurrentQuota());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime date = LocalDateTime.now();
        ClassSession cs = new ClassSession("CS102", "Martin", date, 30);
        assertEquals("CS102", cs.getId());
        assertEquals("Martin", cs.getProfessor());
        assertEquals(date, cs.getDate());
        assertEquals(30, cs.getMaxQuota());
        assertEquals(0, cs.getCurrentQuota());
    }

    @Test
    void testAddStudent() {
        classSession.addStudent(student1);
        assertEquals(1, classSession.getCurrentQuota());

        classSession.addStudent(student2);
        assertEquals(2, classSession.getCurrentQuota());
    }

    @Test
    void testAddStudentExceedsQuota() {
        classSession.addStudent(student1);
        classSession.addStudent(student2);

        Student student3 = new Student("Carlos", "11111");
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> classSession.addStudent(student3));
        assertEquals("No hay cupos", exception.getMessage());
    }

    @Test
    void testDelStudent() {
        classSession.addStudent(student1);
        classSession.addStudent(student2);
        assertEquals(2, classSession.getCurrentQuota());

        classSession.delStudent("12345");
        assertEquals(1, classSession.getCurrentQuota());

        classSession.delStudent("67890");
        assertEquals(0, classSession.getCurrentQuota());
    }

    @Test
    void testDelNonExistentStudent() {
        classSession.addStudent(student1);
        classSession.delStudent("99999");
        assertEquals(1, classSession.getCurrentQuota());
    }

    @Test
    void testGettersWithConstructor() {
        assertNotNull(classSession.getId());
        assertNotNull(classSession.getProfessor());
        assertNotNull(classSession.getDate());
        assertEquals(2, classSession.getMaxQuota());
    }
}

