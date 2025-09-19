package edu.dosw.sirha.observer;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.services.DecanaturaNotificationService;

public class DecanaturaObserver implements ClassSessionObserver {
    private DecanaturaNotificationService notificationService;
    private String facultyCode;

    public DecanaturaObserver(String facultyCode) {
        this.facultyCode = facultyCode;
        this.notificationService = new DecanaturaNotificationService();
    }

    @Override
    public void update(ClassSession classSession, String eventType, Object data) {
        if (shouldNotifyDecanatura(eventType)) {
            String message = createDecanaturaMessage(classSession, eventType, data);
            notificationService.sendNotification(facultyCode, message, eventType);
        }
    }

    private boolean shouldNotifyDecanatura(String eventType) {
        return eventType.equals(ClassSession.EVENT_QUOTA_FULL) ||
                eventType.equals(ClassSession.EVENT_QUOTA_WARNING);
    }

    private String createDecanaturaMessage(ClassSession classSession, String eventType, Object data) {
        return switch (eventType) {
            case ClassSession.EVENT_QUOTA_FULL ->
                    String.format("ALERTA: Grupo %s del profesor %s ha alcanzado cupo máximo (%d estudiantes)", classSession.getId(), classSession.getProfessor(), classSession.getMaxQuota());
            case ClassSession.EVENT_QUOTA_WARNING ->
                    String.format("ADVERTENCIA: Grupo %s está al %.0f%% de capacidad (%d/%d)", classSession.getId(), classSession.getOccupancyPercentage(), classSession.getCurrentQuota(), classSession.getMaxQuota());
            default -> "Notificación del grupo " + classSession.getId() + ": " + eventType;
        };
    }
}
