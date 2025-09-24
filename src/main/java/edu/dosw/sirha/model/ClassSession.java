

package edu.dosw.sirha.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import edu.dosw.sirha.services.observer.ClassSessionObserver;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Class representing a class session with students and quota management.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSession {
    private String id;
    private String professor;
    private LocalDateTime date;
    private int maxQuota;
    private int currentQuota = 0;
    private ArrayList<Student> students = new ArrayList<>();
    private List<ClassSessionObserver> observers = new ArrayList<>();

    public static final String EVENT_STUDENT_ADDED = "STUDENT_ADDED";
    public static final String EVENT_STUDENT_REMOVED = "STUDENT_REMOVED";
    public static final String EVENT_QUOTA_FULL = "QUOTA_FULL";
    public static final String EVENT_QUOTA_WARNING = "QUOTA_90_PERCENT";
    public static final String EVENT_QUOTA_AVAILABLE = "QUOTA_AVAILABLE";

    /**
     * Parameterized constructor to initialize a class session.
     *
     * @param id        the unique identifier of the class session
     * @param professor the name of the professor
     * @param date      the date and time of the class session
     * @param maxQuota  the maximum number of students allowed
     */
    public ClassSession(String id, String professor, LocalDateTime date, int maxQuota) {
        this.id = id;
        this.professor = professor;
        this.date = date;
        this.maxQuota = maxQuota;
    }

    /**
     * Adds an observer to the class session.
     *
     * @param observer the observer to be added
     */
    public void addObserver(ClassSessionObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer from the class session.
     *
     * @param observer the observer to be removed
     */
    public void removeObserver(ClassSessionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all observers of a specific event.
     *
     * @param eventType the type of event
     * @param data      additional data related to the event
     */
    private void notifyObservers(String eventType, Object data) {
        for (ClassSessionObserver observer : observers) {
            observer.update(this, eventType, data);
        }
    }

    /**
     * Adds a student to the class session if there is available quota.
     *
     * @param student the student to be added
     */
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

    /**
     * Removes a student from the class session by their ID.
     *
     * @param studentId the ID of the student to be removed
     */
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

    /**
     * Checks if there is available quota in the class session.
     *
     * @return true if there is available quota, false otherwise
     */
    public boolean hasAvailableQuota() {
        return currentQuota < maxQuota;
    }

    /**
     * Gets the number of available quota slots.
     *
     * @return the number of available quota slots
     */
    public int getAvailableQuota() {
        return maxQuota - currentQuota;
    }

    /**
     * Gets the occupancy percentage of the class session.
     *
     * @return the occupancy percentage
     */
    public double getOccupancyPercentage() {
        return maxQuota > 0 ? (double) currentQuota / maxQuota * 100 : 0;
    }
}
