package edu.dosw.sirha.services.observer;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.services.DeanNotificationService;

/**
 * Observer that notifies the Dean about important events in class sessions.
 */
public class DeanObserver implements ClassSessionObserver {
    private DeanNotificationService notificationService;
    private String facultyCode;

    /**
     * Constructor for DeanObserver.
     * @param facultyCode The code of the faculty to which the Dean belongs.
     */
    public DeanObserver(String facultyCode) {
        this.facultyCode = facultyCode;
        this.notificationService = new DeanNotificationService();
    }

    /**
     * Updates the Dean with information about class session events.
     * @param classSession the class session that generated the event
     * @param eventType the type of event (e.g., "QUOTA_EXCEEDED", "STUDENT_ADDED")
     * @param data additional data related to the event
     */
    @Override
    public void update(ClassSession classSession, String eventType, Object data) {
        if (shouldNotifyDecanatura(eventType)) {
            String message = createDecanaturaMessage(classSession, eventType, data);
            notificationService.sendNotification(facultyCode, message, eventType);
        }
    }

    /**
     * Determines if the Dean should be notified based on the event type.
     * @param eventType the type of event
     * @return true if the Dean should be notified, false otherwise
     */
    private boolean shouldNotifyDecanatura(String eventType) {
        return eventType.equals(ClassSession.EVENT_QUOTA_FULL) || eventType.equals(ClassSession.EVENT_QUOTA_WARNING);
    }

    /**
     * Creates a message for the Dean based on the event type and class session details.
     * @param classSession the class session that generated the event
     * @param eventType the type of event
     * @param data additional data related to the event
     * @return the message to be sent to the Dean
     */
    private String createDecanaturaMessage(ClassSession classSession, String eventType, Object data) {
        return switch (eventType) {
            case ClassSession.EVENT_QUOTA_FULL -> String.format("ALERTA: Grupo %s del profesor %s ha alcanzado cupo máximo (%d estudiantes)", classSession.getId(), classSession.getProfessor(), classSession.getMaxQuota());
            case ClassSession.EVENT_QUOTA_WARNING -> String.format("ADVERTENCIA: Grupo %s está al %.0f%% de capacidad (%d/%d)", classSession.getId(), classSession.getOccupancyPercentage(), classSession.getCurrentQuota(), classSession.getMaxQuota());
            default -> "Notificación del grupo " + classSession.getId() + ": " + eventType;
        };
    }
}
