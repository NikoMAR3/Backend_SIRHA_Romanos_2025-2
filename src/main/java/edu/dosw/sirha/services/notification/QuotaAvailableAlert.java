package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

/**
 * QuotaAvailableAlert class representing an alert for available quota in a class session.
 * Inherits from BaseAlert and sets the type to "QUOTA_AVAILABLE".
 */
public class QuotaAvailableAlert extends BaseAlert {
    private ClassSession classSession;

    /**
     * Constructor for QuotaAvailableAlert.
     *
     * @param classSession The class session with available quota.
     */
    public QuotaAvailableAlert(ClassSession classSession) {
        super("Hay cupos disponibles en el grupo " + classSession.getId() + " (" + (classSession.getMaxQuota() - classSession.getCurrentQuota()) + " cupos libres)", "QUOTA_AVAILABLE", "INFO");
        this.classSession = classSession;
    }
}