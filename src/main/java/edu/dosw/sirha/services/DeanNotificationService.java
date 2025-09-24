package edu.dosw.sirha.services;

/**
 * Service for sending notifications to the dean's.
 */
public class DeanNotificationService {

    /**
     * Sends a notification to the dean's.
     * @param facultyCode the code of the faculty to which the notification is sent
     * @param message the content of the notification
     * @param eventType the type of event triggering the notification
     */
    public void sendNotification(String facultyCode, String message, String eventType) {
        System.out.println("=== NOTIFICACIÓN DECANATURA [" + facultyCode + "]=== :");
        System.out.println("- Tipo: " + eventType);
        System.out.println("- Mensaje: " + message);
    }
}