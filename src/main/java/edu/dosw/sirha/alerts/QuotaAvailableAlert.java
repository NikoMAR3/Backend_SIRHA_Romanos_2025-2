package edu.dosw.sirha.alerts;

import edu.dosw.sirha.model.ClassSession;

public class QuotaAvailableAlert extends BaseAlert {
    private ClassSession classSession;

    public QuotaAvailableAlert(ClassSession classSession) {
        super("Hay cupos disponibles en el grupo " + classSession.getId() + " (" + (classSession.getMaxQuota() - classSession.getCurrentQuota()) + " cupos libres)", "QUOTA_AVAILABLE", "INFO");
        this.classSession = classSession;
    }
}