package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

public class CapacityWarningAlert extends BaseAlert {
    private ClassSession classSession;

    public CapacityWarningAlert(ClassSession classSession) {
        super("Advertencia: El grupo " + classSession.getId() + " está cerca del límite de capacidad", "CAPACITY_WARNING", "WARNING");
        this.classSession = classSession;
    }
}