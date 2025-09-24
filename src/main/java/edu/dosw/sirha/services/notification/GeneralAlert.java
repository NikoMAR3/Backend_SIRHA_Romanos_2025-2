package edu.dosw.sirha.services.notification;

/**
 * GeneralAlert class representing a general notification alert.
 * Inherits from BaseAlert and sets the type to "GENERAL".
 */
public class GeneralAlert extends BaseAlert {
    public GeneralAlert(String message, String level) {
        super(message, "GENERAL", level);
    }
}