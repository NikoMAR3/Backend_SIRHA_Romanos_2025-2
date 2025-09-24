package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

/**
 * QuotaFullAlert class representing an alert for a full quota in a class session.
 * Inherits from BaseAlert and sets the type to "QUOTA_FULL".
 */
public class QuotaFullAlert extends BaseAlert {
    private ClassSession classSession;

    /**
     * Constructor for QuotaFullAlert.
     * @param classSession The class session that has reached full quota.
     */
    public QuotaFullAlert(ClassSession classSession) {
        super("El grupo " + classSession.getId() + " ha alcanzado su cupo máximo (" + classSession.getMaxQuota() + " estudiantes)", "QUOTA_FULL", "CRITICAL");
        this.classSession = classSession;
    }

    /**
     * Sends the alert and notifies the dean.
     */
    @Override
    public void send() {
        super.send();
        notifyDean();
    }

    /**
     * Notifies the dean about the full quota situation.
     */
    private void notifyDean() {
        System.out.println("Notificando a decanatura grupo lleno: " + classSession.getId());
    }
}