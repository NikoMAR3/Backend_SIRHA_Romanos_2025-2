package edu.dosw.sirha.observer;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.factory.AlertFactory;
import java.util.ArrayList;
import java.util.List;

public class ClassQuotaObserver implements ClassSessionObserver {
    private List<Alert> alerts;
    private AlertFactory quotaAlertFactory;
    private AlertFactory capacityAlertFactory;

    public ClassQuotaObserver() {
        this.alerts = new ArrayList<>();
        this.quotaAlertFactory = AlertFactory.getFactory("quota");
        this.capacityAlertFactory = AlertFactory.getFactory("capacity");
    }

    @Override
    public void update(ClassSession classSession, String eventType, Object data) {
        Alert alert = createAlert(classSession, eventType, data);
        if (alert != null) {
            alerts.add(alert);
            alert.send();
        }
    }

    private Alert createAlert(ClassSession classSession, String eventType, Object data) {
        try {
            if (eventType.contains("QUOTA")) {
                return quotaAlertFactory.createAlert(classSession, eventType, data);
            } else if (eventType.contains("STUDENT") || eventType.contains("CAPACITY")) {
                return capacityAlertFactory.createAlert(classSession, eventType, data);
            } else {
                return AlertFactory.getFactory("general").createAlert(classSession, eventType, data);
            }
        } catch (Exception e) {
            System.err.println("Error al crear la alerta: " + e.getMessage());
            return null;
        }
    }

    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }

    public void clearAlerts() {
        alerts.clear();
    }
}