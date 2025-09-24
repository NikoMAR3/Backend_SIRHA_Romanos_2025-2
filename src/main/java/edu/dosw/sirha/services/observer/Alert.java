package edu.dosw.sirha.services.observer;

/**
 * Interface representing an alert in the system.
 * Alerts can be of various types and levels, and can be sent to notify users or systems.
 */
public interface Alert {
    String getMessage();
    String getType();
    String getLevel();
    long getTimestamp();
    void send();
}