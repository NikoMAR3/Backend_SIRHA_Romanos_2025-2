package edu.dosw.sirha.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * Class representing a traffic light system for academic progress.
 */
@Getter
@Setter
@NoArgsConstructor
public class TrafficLight {
    private String major;
    private ArrayList<Subject> subjectsApproved = new ArrayList<>();
    private ArrayList<Subject> subjectsFailed = new ArrayList<>();
    private ArrayList<Subject> allSubjects = new ArrayList<>();

    /**
     * Parameterized constructor to initialize a traffic light with a major.
     *
     * @param major the academic major
     */
    public TrafficLight(String major) {
        this.major = major;
    }

    /**
     * Calculates the current status of the traffic light based on approved credits.
     * @return the status color ("VERDE", "AZUL", "ROJO")
     */
    public String getStatus() {
        double totalCredits = allSubjects.stream().mapToInt(Subject::getCredits).sum();
        double approvedCredits = subjectsApproved.stream().mapToInt(Subject::getCredits).sum();
        double progress = (approvedCredits / totalCredits) * 100;

        if (progress >= 80) return "VERDE";
        if (progress >= 50) return "AZUL";
        return "ROJO";
    }

    /**
     * Creates a default traffic light instance.
     * @return a TrafficLight object with default values
     */
    public static TrafficLight getDefaultTrafficLight() {
        return new TrafficLight();
    }
}
