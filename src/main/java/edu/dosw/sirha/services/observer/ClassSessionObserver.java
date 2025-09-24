package edu.dosw.sirha.services.observer;

import edu.dosw.sirha.model.ClassSession;

public interface ClassSessionObserver {
    void update(ClassSession classSession, String eventType, Object data);
}