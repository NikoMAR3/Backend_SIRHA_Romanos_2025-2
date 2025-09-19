package edu.dosw.sirha.observer;

public interface Alert {
    String getMessage();
    String getType();
    String getLevel();
    long getTimestamp();
    void send();
}