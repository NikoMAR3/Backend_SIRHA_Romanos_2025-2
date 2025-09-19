package edu.dosw.sirha.observer;

import edu.dosw.sirha.model.ClassSession;

public interface ClassSessionObserver {
    void update(ClassSession classSession, String eventType, Object data);
}