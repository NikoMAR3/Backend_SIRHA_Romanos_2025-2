package edu.dosw.sirha.model;

import java.util.ArrayList;

public class TrafficLight {
    private String major;
    private ArrayList<Subject> subjectsApproved = new ArrayList<>();
    private ArrayList<Subject> subjectsFailed = new ArrayList<>();
    private ArrayList<Subject> allSubjects = new ArrayList<>();

    public TrafficLight(String major) {
        this.major = major;
    }

    public String getMajor() { return major; }
    public ArrayList<Subject> getSubjectsApproved() { return subjectsApproved; }
    public ArrayList<Subject> getSubjectsFailed() { return subjectsFailed; }
    public ArrayList<Subject> getAllSubjects() { return allSubjects; }

    public String getStatus() {
        double totalCredits = allSubjects.stream().mapToInt(Subject::getCredits).sum();
        double approvedCredits = subjectsApproved.stream().mapToInt(Subject::getCredits).sum();
        double progress = (approvedCredits / totalCredits) * 100;

        if (progress >= 80) return "VERDE";
        if (progress >= 50) return "AZUL";
        return "ROJO";
    }
}
