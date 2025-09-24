package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.services.observer.Alert;

/**
 * Base implementation of the Alert interface.
 * Provides common properties and methods for different types of alerts.
 */
public abstract class BaseAlert implements Alert {
    protected String message;
    protected String type;
    protected String level;
    protected long timestamp;

    /**
     * Constructor to initialize the alert with message, type, and level.
     * @param message
     * @param type
     * @param level
     */
    public BaseAlert(String message, String type, String level) {
        this.message = message;
        this.type = type;
        this.level = level;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the alert message.
     * @return the alert message
     */
    @Override
    public String getMessage() { return message; }

    /**
     * Get the alert type.
     * @return the alert type
     */
    @Override
    public String getType() { return type; }

    /**
     * Get the alert level.
     * @return the alert level
     */
    @Override
    public String getLevel() { return level; }

    /**
     * Get the timestamp when the alert was created.
     * @return the timestamp
     */
    @Override
    public long getTimestamp() { return timestamp; }

    /**
     * Send the alert.
     */
    @Override
    public void send() {
        System.out.println("[" + level + "] " + type + ": " + message);
    }
}