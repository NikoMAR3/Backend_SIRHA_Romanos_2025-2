package edu.dosw.sirha.factory;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.observer.Alert;
import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.alerts.*;

public class CapacityAlertFactory extends AlertFactory {
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