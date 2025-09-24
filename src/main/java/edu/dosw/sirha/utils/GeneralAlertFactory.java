package edu.dosw.sirha.utils;

import edu.dosw.sirha.services.notification.GeneralAlert;
import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.model.ClassSession;

public class GeneralAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(ClassSession classSession, String eventType, Object data) {
        return new GeneralAlert("Evento en sesión " + classSession.getId() + ": " + eventType, "INFO");
    }
}
