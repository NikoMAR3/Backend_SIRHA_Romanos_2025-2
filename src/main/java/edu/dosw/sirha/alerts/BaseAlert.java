package edu.dosw.sirha.alerts;

import edu.dosw.sirha.observer.Alert;

public abstract class BaseAlert implements Alert {
    protected String message;
    protected String type;
    protected String level;
    protected long timestamp;

    public BaseAlert(String message, String type, String level) {
        this.message = message;
        this.type = type;
        this.level = level;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getMessage() { return message; }

    @Override
    public String getType() { return type; }

    @Override
    public String getLevel() { return level; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public void send() {
        System.out.println("[" + level + "] " + type + ": " + message);
    }
}