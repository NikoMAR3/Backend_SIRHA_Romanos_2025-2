package edu.dosw.sirha.model;

import lombok.Getter;
import edu.dosw.sirha.observer.ClassSessionObserver;

import java.time.LocalDateTime;
import java.util.*;

public class ClassSession {
    @Getter
    private String id;
    @Getter
    private String professor;
    @Getter
    private LocalDateTime date;
    @Getter
    private int maxQuota;
    @Getter
    private int currentQuota = 0;
    @Getter
    private ArrayList<Student> students = new ArrayList<>();

    private List<ClassSessionObserver> observers = new ArrayList<>();

    public static final String EVENT_STUDENT_ADDED = "STUDENT_ADDED";
    public static final String EVENT_STUDENT_REMOVED = "STUDENT_REMOVED";
    public static final String EVENT_QUOTA_FULL = "QUOTA_FULL";
    public static final String EVENT_QUOTA_WARNING = "QUOTA_90_PERCENT";
    public static final String EVENT_QUOTA_AVAILABLE = "QUOTA_AVAILABLE";

    public ClassSession() {}

    public ClassSession(String id, String professor, LocalDateTime date, int maxQuota) {
        this.id = id;
        this.professor = professor;
        this.date = date;
        this.maxQuota = maxQuota;
    }

    public void addObserver(ClassSessionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ClassSessionObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String eventType, Object data) {
        for (ClassSessionObserver observer : observers) {
            observer.update(this, eventType, data);
        }
    }

    public void addStudent(Student student) {
        if (currentQuota >= maxQuota) {
            notifyObservers(EVENT_QUOTA_FULL, student);
            throw new IllegalStateException("No hay cupos disponibles");
        }

        this.students.add(student);
        this.currentQuota++;

        notifyObservers(EVENT_STUDENT_ADDED, student);

        double percentage = (double) currentQuota / maxQuota * 100;
        if (percentage >= 90 && currentQuota < maxQuota) {
            notifyObservers(EVENT_QUOTA_WARNING, percentage);
        }

        if (currentQuota == maxQuota) {
            notifyObservers(EVENT_QUOTA_FULL, null);
        }
    }

    public void delStudent(String studentId) {
        boolean wasRemoved = students.removeIf(s -> s.getId().equals(studentId));
        if (wasRemoved) {
            int oldQuota = currentQuota;
            currentQuota = students.size();

            notifyObservers(EVENT_STUDENT_REMOVED, studentId);

            if (oldQuota == maxQuota && currentQuota < maxQuota) {
                notifyObservers(EVENT_QUOTA_AVAILABLE, null);
            }
        }
    }

    public boolean hasAvailableQuota() {
        return currentQuota < maxQuota;
    }

    public int getAvailableQuota() {
        return maxQuota - currentQuota;
    }

    public double getOccupancyPercentage() {
        return maxQuota > 0 ? (double) currentQuota / maxQuota * 100 : 0;
    }
}