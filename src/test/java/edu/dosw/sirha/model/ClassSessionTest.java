package edu.dosw.sirha.model;

import edu.dosw.sirha.observer.ClassSessionObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassSessionTest {
    private ClassSession classSession;
    private Student student1;
    private Student student2;
    @Mock
    private ClassSessionObserver mockObserver;
    @Mock
    private Student mockStudent;

    @BeforeEach
    void setUp() {
        classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 2);
        student1 = new Student("Juan", "12345");
        student2 = new Student("Maria", "67890");

        MockitoAnnotations.openMocks(this);
        classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);
        when(mockStudent.getId()).thenReturn("202012345");
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
    //Verifica que al agregar estudiantes el cupo se incremente
    void testAddStudent() {
        classSession.addStudent(student1);
        assertEquals(1, classSession.getCurrentQuota());

        classSession.addStudent(student2);
        assertEquals(2, classSession.getCurrentQuota());
    }

    @Test
    //verifica que se lance la excepción cuando se supere el limite
    void testAddStudentExceedsQuota() {
        classSession.addStudent(student1);
        classSession.addStudent(student2);

        Student student3 = new Student("Carlos", "11111");
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> classSession.addStudent(student3));
        assertEquals("No hay cupos", exception.getMessage());
    }

    @Test
    //Verifica que al eliminar un estudiante el cupo se disminuya
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
    //verifica que si se elimina un estudiante que no existe no se afecte el cupo
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

    @Test
    //verifica que al agregar un estudiante se le notifique al observer
    void shouldAddNormalStudent() {
        classSession.addStudent(mockStudent);

        assertEquals(1, classSession.getCurrentQuota());
        assertTrue(classSession.getStudents().contains(mockStudent));
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_ADDED, mockStudent);
    }

    @Test
    //verifica que si el cupo esta lleno se notifique al observer
    void shouldThrowsExceptionWhenQuotaFull() {
        for (int i = 0; i < 5; i++) {
            Student student = mock(Student.class);
            when(student.getId()).thenReturn("20201234" + i);
            classSession.addStudent(student);
        }

        Student extraStudent = mock(Student.class);
        when(extraStudent.getId()).thenReturn("202012350");

        assertThrows(IllegalStateException.class, () -> {
            classSession.addStudent(extraStudent);
        });
        verify(mockObserver).update(classSession, ClassSession.EVENT_QUOTA_FULL, extraStudent);
    }

    @Test
    //verifica que si se llega al 90% del cupo se notifique al observer
    void shouldThrow90PercentAlert() {
        for (int i = 0; i < 4; i++) {
            Student student = mock(Student.class);
            when(student.getId()).thenReturn("20201234" + i);
            classSession.addStudent(student);
        }
        reset(mockObserver);
        verify(mockObserver, never()).update(eq(classSession), eq(ClassSession.EVENT_QUOTA_WARNING), any());

        ClassSession session10 = new ClassSession("TEST-10", "Prof", LocalDateTime.now(), 10);
        ClassSessionObserver observer = mock(ClassSessionObserver.class);
        session10.addObserver(observer);

        for (int i = 0; i < 9; i++) {
            Student student = mock(Student.class);
            when(student.getId()).thenReturn("test" + i);
            session10.addStudent(student);
        }

        verify(observer).update(eq(session10), eq(ClassSession.EVENT_QUOTA_WARNING), any());
    }

    @Test
    //verifica que al eliminar un estudiante se le notifique al observer
    void shouldDeleteStudent() {
        classSession.addStudent(mockStudent);
        reset(mockObserver);

        classSession.delStudent("202012345");

        assertEquals(0, classSession.getCurrentQuota());
        assertFalse(classSession.getStudents().contains(mockStudent));
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_REMOVED, "202012345");
    }

    @Test
    //verifica que cuando una clase llena libere un cupo se le notifique
    void shouldNotifyFreeQuota() {
        for (int i = 0; i < 5; i++) {
            Student student = mock(Student.class);
            when(student.getId()).thenReturn("20201234" + i);
            classSession.addStudent(student);
        }
        reset(mockObserver);
        classSession.delStudent("202012340");

        assertEquals(4, classSession.getCurrentQuota());
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_REMOVED, "202012340");
        verify(mockObserver).update(classSession, ClassSession.EVENT_QUOTA_AVAILABLE, null);
    }

    @Test
    //verifica que haya cupos disponibles, calcular cupos y el porcentaje que esta ocupado
    void testUtilityMethods() {
        assertTrue(classSession.hasAvailableQuota());
        assertEquals(5, classSession.getAvailableQuota());
        assertEquals(0.0, classSession.getOccupancyPercentage());

        classSession.addStudent(mockStudent);

        assertEquals(4, classSession.getAvailableQuota());
        assertEquals(20.0, classSession.getOccupancyPercentage());
    }

    
    @Test
    //verifica que despues de quitar el observer ya no reciba notificaciones
    void testObserverManagement() {
        classSession.removeObserver(mockObserver);
        classSession.addStudent(mockStudent);

        verifyNoInteractions(mockObserver);
    }
}

