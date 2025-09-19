package edu.dosw.sirha.factory;

import edu.dosw.sirha.observer.Alert;
import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.alerts.*;

public class GeneralAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(ClassSession classSession, String eventType, Object data) {
        return new GeneralAlert("Evento en sesión " + classSession.getId() + ": " + eventType, "INFO");
    }
}
