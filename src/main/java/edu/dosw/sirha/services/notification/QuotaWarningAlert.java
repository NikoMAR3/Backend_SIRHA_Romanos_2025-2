package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

/**
 * QuotaWarningAlert class representing an alert for a quota warning in a class session.
 * Inherits from BaseAlert and sets the type to "QUOTA_WARNING".
 */
public class QuotaWarningAlert extends BaseAlert {
    private ClassSession classSession;
    private int percentage;

    /**
     * Constructor for QuotaWarningAlert.
     *
     * @param classSession The class session that is nearing its quota limit.
     * @param percentage   The percentage of the quota that has been reached.
     */
    public QuotaWarningAlert(ClassSession classSession, int percentage) {
        super("El grupo " + classSession.getId() + " ha alcanzado el " + percentage + "% de su capacidad (" + classSession.getCurrentQuota() + "/" + classSession.getMaxQuota() + ")", "QUOTA_WARNING", "WARNING");
        this.classSession = classSession;
        this.percentage = percentage;
    }
}