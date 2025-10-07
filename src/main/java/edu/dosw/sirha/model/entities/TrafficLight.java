package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Entity that represents a traffic light status for a student in a specific program and subject.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trafficLights")
public class TrafficLight {
    @Id
    private String id;

    private String studentId;
    private String programId;
    private String subjectShortName;
    private String subjectName;

    private HashMap<Subject, Integer> failedSubjects = new HashMap<>();
    private HashMap<Subject, Integer> approvedSubjects = new HashMap<>();
    private List<Subject> onGoingSubjects = new ArrayList<>();
    private List<Subject> unseenSubjects = new ArrayList<>();
    private int semester;
    private TrafficLightStatus status;
    private double grade;
    private int credits;
}