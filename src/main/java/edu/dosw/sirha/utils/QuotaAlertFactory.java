package edu.dosw.sirha.utils;

import edu.dosw.sirha.services.notification.GeneralAlert;
import edu.dosw.sirha.services.notification.QuotaAvailableAlert;
import edu.dosw.sirha.services.notification.QuotaFullAlert;
import edu.dosw.sirha.services.notification.QuotaWarningAlert;
import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.model.ClassSession;

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