package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

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
    private List<Subject> failedSubjects = new ArrayList<>();
    private List<Subject> approvedSubjects = new ArrayList<>();
    private List<Subject> onGoingSubjects = new ArrayList<>();
    private List<Subject> unseenSubjects = new ArrayList<>();
    private int semester;
    private TrafficLightStatus status;
    private double grade;
    private int credits;
}