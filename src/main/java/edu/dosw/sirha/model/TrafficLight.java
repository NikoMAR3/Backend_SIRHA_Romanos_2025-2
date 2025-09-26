package edu.dosw.sirha.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a traffic light system for academic progress.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "traffic_lights")
public class TrafficLight {
    @Id
    private String id;
    @Field("studentId")
    private String studentId;
    //@Field("subjects")
    //private List<SubjectGrade> subjects = new ArrayList<>();
    @Field("state")
    private String state;

    private transient String major;
    private transient ArrayList<Subject> subjectsApproved = new ArrayList<>();
    private transient ArrayList<Subject> subjectsFailed = new ArrayList<>();
    private transient ArrayList<Subject> allSubjects = new ArrayList<>();

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
