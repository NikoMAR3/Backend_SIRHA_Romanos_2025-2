package edu.dosw.sirha.services;

public class DecanaturaNotificationService {
    public void sendNotification(String facultyCode, String message, String eventType) {
        System.out.println("=== NOTIFICACIÓN DECANATURA [" + facultyCode + "]=== :");
        System.out.println("- Tipo: " + eventType);
        System.out.println("- Mensaje: " + message);
    }
}