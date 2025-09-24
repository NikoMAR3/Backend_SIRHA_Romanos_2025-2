package edu.dosw.sirha.model;

import edu.dosw.sirha.services.observer.ClassSessionObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for testing the functionality of the ClassSession class.
 * This test class verifies proper initialization, student management,
 * quota control, observer pattern implementation, and various utility methods
 * of ClassSession instances using JUnit 5 and Mockito framework.
 */
class ClassSessionTest {
    private Student student1;
    private Student student2;
    @Mock
    private ClassSessionObserver mockObserver;
    @Mock
    private Student mockStudent;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes mock objects and creates test Student instances
     * for use across multiple test methods.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student1 = new Student("Juan", "12345");
        student2 = new Student("Maria", "67890");
        when(mockStudent.getId()).thenReturn("202012345");
    }

    /**
     * Tests the default constructor of ClassSession.
     * Verifies that the instance is properly created and
     * the current quota is initialized to zero.
     */
    @Test
    void testDefaultConstructor() {
        ClassSession cs = new ClassSession();
        assertNotNull(cs);
        assertEquals(0, cs.getCurrentQuota());
    }

    /**
     * Tests the parameterized constructor of ClassSession.
     * Verifies that all parameters are properly assigned and
     * the current quota starts at zero while max quota is set correctly.
     */
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

    /**
     * Tests the addStudent functionality.
     * Verifies that students can be added to the class session
     * and the current quota is updated accordingly.
     */
    @Test
    void testAddStudent() {
        ClassSession classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 5);

        classSession.addStudent(student1);
        assertEquals(1, classSession.getCurrentQuota());

        classSession.addStudent(student2);
        assertEquals(2, classSession.getCurrentQuota());
    }

    /**
     * Tests the quota limit enforcement when adding students.
     * Verifies that an IllegalStateException is thrown when
     * attempting to add a student beyond the maximum quota.
     */
    @Test
    void testAddStudentExceedsQuota() {
        ClassSession classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 2);

        classSession.addStudent(student1);
        classSession.addStudent(student2);

        Student student3 = new Student("Carlos", "11111");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> classSession.addStudent(student3));
        assertEquals("No hay cupos disponibles", exception.getMessage());
    }

    /**
     * Tests the delStudent functionality.
     * Verifies that students can be removed from the class session
     * and the current quota is decremented properly.
     */
    @Test
    void testDelStudent() {
        ClassSession classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 5);

        classSession.addStudent(student1);
        classSession.addStudent(student2);
        assertEquals(2, classSession.getCurrentQuota());

        classSession.delStudent("12345");
        assertEquals(1, classSession.getCurrentQuota());

        classSession.delStudent("67890");
        assertEquals(0, classSession.getCurrentQuota());
    }

    /**
     * Tests the behavior when attempting to delete a non-existent student.
     * Verifies that the quota remains unchanged when trying to remove
     * a student that is not enrolled in the class session.
     */
    @Test
    void testDelNonExistentStudent() {
        ClassSession classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 5);

        classSession.addStudent(student1);
        classSession.delStudent("99999");
        assertEquals(1, classSession.getCurrentQuota());
    }

    /**
     * Tests all getter methods with constructor initialization.
     * Verifies that all properties are properly accessible
     * through their respective getter methods.
     */
    @Test
    void testGettersWithConstructor() {
        ClassSession classSession = new ClassSession("CS101", "Dr. Smith", LocalDateTime.now(), 2);

        assertNotNull(classSession.getId());
        assertNotNull(classSession.getProfessor());
        assertNotNull(classSession.getDate());
        assertEquals(2, classSession.getMaxQuota());
    }

    /**
     * Tests adding a normal student with observer notification.
     * Verifies that when a student is added, the observer is notified
     * with the appropriate event and the student is properly enrolled.
     */
    @Test
    void shouldAddNormalStudent() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);

        classSession.addStudent(mockStudent);

        assertEquals(1, classSession.getCurrentQuota());
        assertTrue(classSession.getStudents().contains(mockStudent));
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_ADDED, mockStudent);
    }

    /**
     * Tests exception handling when quota is full.
     * Verifies that an IllegalStateException is thrown and
     * the observer is notified when attempting to exceed the quota limit.
     */
    @Test
    void shouldThrowsExceptionWhenQuotaFull() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);

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

    /**
     * Tests the 90% quota warning notification system.
     * Verifies that observers are notified when the class session
     * reaches 90% of its maximum capacity.
     */
    @Test
    void shouldThrow90PercentAlert() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);

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

    /**
     * Tests student deletion with observer notification.
     * Verifies that when a student is removed, the observer is notified
     * and the student is no longer in the enrolled students list.
     */
    @Test
    void shouldDeleteStudent() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);

        classSession.addStudent(mockStudent);
        reset(mockObserver);

        classSession.delStudent("202012345");

        assertEquals(0, classSession.getCurrentQuota());
        assertFalse(classSession.getStudents().contains(mockStudent));
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_REMOVED, "202012345");
    }

    /**
     * Tests quota availability notification after student removal.
     * Verifies that when a student is removed from a full class,
     * observers are notified about both the removal and quota availability.
     */
    @Test
    void shouldNotifyFreeQuota() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);

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

    /**
     * Tests utility methods for quota management.
     * Verifies the correct behavior of hasAvailableQuota(), getAvailableQuota(),
     * and getOccupancyPercentage() methods under different scenarios.
     */
    @Test
    void testUtilityMethods() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);

        assertTrue(classSession.hasAvailableQuota());
        assertEquals(5, classSession.getAvailableQuota());
        assertEquals(0.0, classSession.getOccupancyPercentage());

        classSession.addStudent(mockStudent);

        assertEquals(4, classSession.getAvailableQuota());
        assertEquals(20.0, classSession.getOccupancyPercentage());
    }

    /**
     * Tests observer management functionality.
     * Verifies that observers can be added and removed properly,
     * and that removed observers no longer receive notifications.
     */
    @Test
    void testObserverManagement() {
        ClassSession classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);

        classSession.removeObserver(mockObserver);
        classSession.addStudent(mockStudent);

        verifyNoInteractions(mockObserver);
    }
}
