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
    @Mock
    private ClassSessionObserver mockObserver;
    @Mock
    private Student mockStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        classSession = new ClassSession("CVDS-G01", "Prof. García", LocalDateTime.now(), 5);
        classSession.addObserver(mockObserver);
        when(mockStudent.getId()).thenReturn("202012345");
    }

    @Test
    void shouldAddNormalStudent() {
        classSession.addStudent(mockStudent);

        assertEquals(1, classSession.getCurrentQuota());
        assertTrue(classSession.getStudents().contains(mockStudent));
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_ADDED, mockStudent);
    }

    @Test
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
    void shouldDeleteStudent() {
        classSession.addStudent(mockStudent);
        reset(mockObserver);

        classSession.delStudent("202012345");

        assertEquals(0, classSession.getCurrentQuota());
        assertFalse(classSession.getStudents().contains(mockStudent));
        verify(mockObserver).update(classSession, ClassSession.EVENT_STUDENT_REMOVED, "202012345");
    }

    @Test
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
    void testUtilityMethods() {
        assertTrue(classSession.hasAvailableQuota());
        assertEquals(5, classSession.getAvailableQuota());
        assertEquals(0.0, classSession.getOccupancyPercentage());

        classSession.addStudent(mockStudent);

        assertEquals(4, classSession.getAvailableQuota());
        assertEquals(20.0, classSession.getOccupancyPercentage());
    }

    @Test
    void testObserverManagement() {
        classSession.removeObserver(mockObserver);
        classSession.addStudent(mockStudent);

        verifyNoInteractions(mockObserver);
    }
}
