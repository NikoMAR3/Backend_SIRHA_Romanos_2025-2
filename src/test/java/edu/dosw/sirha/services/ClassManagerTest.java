package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ClassSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassManagerTest {
    private ClassManager classManager;
    private ClassSession mockClassSession;

    @BeforeEach
    void setUp() {
        classManager = new ClassManager();
        mockClassSession = mock(ClassSession.class);
    }

    @Test
    void testGetClass() {
        String className = "math";
        classManager.getClassSessions().put(className.toLowerCase(), mockClassSession);
        ClassSession result = classManager.getClass(className);
        assertEquals(mockClassSession, result);
    }

    @Test
    void testModifyMaxClassQuota() {
        String className = "math";
        int newQuota = 30;
        classManager.getClassSessions().put(className, mockClassSession);
        classManager.modifyMaxClassQuota(className, newQuota);
        verify(mockClassSession).setCurrentQuota(newQuota);
    }

    @Test
    void testCheckMaxClassQuota() {
        String className = "math";
        int quota = 25;
        when(mockClassSession.getCurrentQuota()).thenReturn(quota);
        classManager.getClassSessions().put(className, mockClassSession);
        int result = classManager.checkMaxClassQuota(className);
        assertEquals(quota, result);
    }

    @Test
    void testCheckClassQuota() {
        String className = "math";
        int quota = 20;
        when(mockClassSession.getId()).thenReturn(className.toLowerCase());
        when(mockClassSession.getCurrentQuota()).thenReturn(quota);

        classManager.getClassSessions().put(className.toLowerCase(), mockClassSession);

        int result = classManager.checkClassQuota(mockClassSession);
        assertEquals(quota, result);
    }


    private HashMap<String, ClassSession> getClassSessions() {
        try {
            java.lang.reflect.Field field = ClassManager.class.getDeclaredField("classSessions");
            field.setAccessible(true);
            return (HashMap<String, ClassSession>) field.get(classManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}