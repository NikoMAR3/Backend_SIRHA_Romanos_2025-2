package edu.dosw.sirha.model;

import java.util.*;

public class Schedule {
    private ArrayList<ClassSession> classes = new ArrayList<>();

    public Schedule() {}

    public ArrayList<ClassSession> getClasses(){ return classes;}

    public Boolean containsClass(String classSession1) {
        return classes.stream().anyMatch(classSession -> classSession.getId().equals(classSession1));
    }
}
