package edu.dosw.sirha.factory;

import edu.dosw.sirha.observer.Alert;
import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.alerts.*;

public class QuotaAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(ClassSession classSession, String eventType, Object data) {
        return switch (eventType) {
            case "QUOTA_FULL" -> new QuotaFullAlert(classSession);
            case "QUOTA_90_PERCENT" -> new QuotaWarningAlert(classSession, 90);
            case "QUOTA_AVAILABLE" -> new QuotaAvailableAlert(classSession);
            default -> new GeneralAlert("Evento de cupo: " + eventType, "INFO");
        };
    }
}