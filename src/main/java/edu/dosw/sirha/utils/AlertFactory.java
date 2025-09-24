package edu.dosw.sirha.utils;

import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.model.ClassSession;

/**
 * Abstract factory class for creating different types of alerts.
 */
public abstract class AlertFactory {

    /**
     * Creates an alert based on the class session, event type, and additional data.
     * @param classSession
     * @param eventType
     * @param data
     * @return Alert
     */
    public abstract Alert createAlert(ClassSession classSession, String eventType, Object data);

    /**
     * Factory method to get the appropriate AlertFactory based on the factory type.
     * @param factoryType - Type of factory ("quota", "capacity", "general")
     * @return AlertFactory instance
     * @throws IllegalArgumentException if the factory type is invalid
     */
    public static AlertFactory getFactory(String factoryType) {
        return switch (factoryType.toLowerCase()) {
            case "quota" -> new QuotaAlertFactory();
            case "capacity" -> new CapacityAlertFactory();
            case "general" -> new GeneralAlertFactory();
            default -> throw new IllegalArgumentException("El tipo ingresado " + factoryType + " no es válido");
        };
    }
}