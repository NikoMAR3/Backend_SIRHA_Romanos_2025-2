package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

public class QuotaWarningAlert extends BaseAlert {
    private ClassSession classSession;
    private int percentage;

    public QuotaWarningAlert(ClassSession classSession, int percentage) {
        super("El grupo " + classSession.getId() + " ha alcanzado el " + percentage + "% de su capacidad (" + classSession.getCurrentQuota() + "/" + classSession.getMaxQuota() + ")", "QUOTA_WARNING", "WARNING");
        this.classSession = classSession;
        this.percentage = percentage;
    }
}