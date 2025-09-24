package edu.dosw.sirha.model;

import java.util.*;

/**
 * Schedule class representing a collection of class sessions.
 */
public class Schedule {
    private ArrayList<ClassSession> classes = new ArrayList<>();


    public Schedule() {}

    public ArrayList<ClassSession> getClasses(){ return classes;}

    /**
     * verifies if the schedule contains a class session with the given ID.
     * @param classSession1 the ID of the class session to check.
     * @return true if the schedule contains the class session, false otherwise.
     */
    public Boolean containsClass(String classSession1) {
        return classes.stream().anyMatch(classSession -> classSession.getId().equals(classSession1));
    }
}
