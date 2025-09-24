package edu.dosw.sirha.services.observer;

public interface Alert {
    String getMessage();
    String getType();
    String getLevel();
    long getTimestamp();
    void send();
}