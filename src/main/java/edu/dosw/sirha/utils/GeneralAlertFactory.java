package edu.dosw.sirha.utils;

import edu.dosw.sirha.services.notification.GeneralAlert;
import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.model.ClassSession;

/**
 * Factory for creating general alerts.
 */
public class GeneralAlertFactory extends AlertFactory {

    /**
     * Creates a general alert for a given class session and event type.
     * @param classSession the class session related to the alert
     * @param eventType the type of event triggering the alert
     * @param data additional data for the alert
     * @return the created alert
     */
    @Override
    public Alert createAlert(ClassSession classSession, String eventType, Object data) {
        return new GeneralAlert("Evento en sesión " + classSession.getId() + ": " + eventType, "INFO");
    }
}
