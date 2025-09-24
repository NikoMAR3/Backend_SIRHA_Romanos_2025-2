package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

public class QuotaFullAlert extends BaseAlert {
    private ClassSession classSession;

    public QuotaFullAlert(ClassSession classSession) {
        super("El grupo " + classSession.getId() + " ha alcanzado su cupo máximo (" + classSession.getMaxQuota() + " estudiantes)", "QUOTA_FULL", "CRITICAL");
        this.classSession = classSession;
    }

    @Override
    public void send() {
        super.send();
        notifyDecanatura();
    }

    private void notifyDecanatura() {
        System.out.println("Notificando a decanatura grupo lleno: " + classSession.getId());
    }
}