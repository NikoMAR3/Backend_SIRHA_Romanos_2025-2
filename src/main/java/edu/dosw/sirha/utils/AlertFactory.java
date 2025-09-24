package edu.dosw.sirha.utils;

import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.model.ClassSession;

public abstract class AlertFactory {
    public abstract Alert createAlert(ClassSession classSession, String eventType, Object data);

    public static AlertFactory getFactory(String factoryType) {
        return switch (factoryType.toLowerCase()) {
            case "quota" -> new QuotaAlertFactory();
            case "capacity" -> new CapacityAlertFactory();
            case "general" -> new GeneralAlertFactory();
            default -> throw new IllegalArgumentException("El tipo ingresado " + factoryType + " no es válido");
        };
    }
}