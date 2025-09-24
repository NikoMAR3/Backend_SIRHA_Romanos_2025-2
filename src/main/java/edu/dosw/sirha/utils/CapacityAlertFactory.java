package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.services.notification.CapacityWarningAlert;
import edu.dosw.sirha.services.notification.GeneralAlert;
import edu.dosw.sirha.services.notification.StudentAddedAlert;
import edu.dosw.sirha.services.notification.StudentRemovedAlert;
import edu.dosw.sirha.services.observer.Alert;
import edu.dosw.sirha.model.ClassSession;

/**
 * Factory for creating capacity-related alerts.
 */
public class CapacityAlertFactory extends AlertFactory {

    /**
     * Creates an alert based on the event type and associated data.
     *
     * @param classSession The class session related to the alert.
     * @param eventType The type of event triggering the alert.
     * @param data Additional data relevant to the alert.
     * @return An instance of Alert corresponding to the event type.
     */
    @Override
    public Alert createAlert(ClassSession classSession, String eventType, Object data) {
        return switch (eventType) {
            case "CAPACITY_WARNING" -> new CapacityWarningAlert(classSession);
            case "STUDENT_ADDED" -> new StudentAddedAlert(classSession, (Student) data);
            case "STUDENT_REMOVED" -> new StudentRemovedAlert(classSession, (String) data);
            default -> new GeneralAlert("Evento de capacidad: " + eventType, "INFO");
        };
    }
}