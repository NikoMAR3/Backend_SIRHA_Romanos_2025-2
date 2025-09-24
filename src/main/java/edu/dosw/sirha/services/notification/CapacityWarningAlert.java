package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

/**
 * Alert indicating that a class session is nearing its capacity limit.
 */
public class CapacityWarningAlert extends BaseAlert {
    private ClassSession classSession;

    /**
     * Constructor to create a CapacityWarningAlert for a specific class session.
     * @param classSession the class session that is nearing capacity
     */
    public CapacityWarningAlert(ClassSession classSession) {
        super("Advertencia: El grupo " + classSession.getId() + " está cerca del límite de capacidad", "CAPACITY_WARNING", "WARNING");
        this.classSession = classSession;
    }
}