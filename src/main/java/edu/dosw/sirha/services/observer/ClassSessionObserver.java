package edu.dosw.sirha.services.observer;

import edu.dosw.sirha.model.ClassSession;

/**
 * Interface representing an observer for class session events.
 * Observers implementing this interface can receive updates about class session events.
 */
public interface ClassSessionObserver {

    /**
     * Method to update the observer with a new event from a class session.
     * @param classSession the class session that generated the event
     * @param eventType the type of event (e.g., "QUOTA_EXCEEDED", "STUDENT_ADDED")
     * @param data additional data related to the event
     */
    void update(ClassSession classSession, String eventType, Object data);
}