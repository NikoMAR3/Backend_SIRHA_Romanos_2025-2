package edu.dosw.sirha.services.notification;

public class GeneralAlert extends BaseAlert {
    public GeneralAlert(String message, String level) {
        super(message, "GENERAL", level);
    }
}