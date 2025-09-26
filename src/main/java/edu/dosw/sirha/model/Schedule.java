package edu.dosw.sirha.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

/**
 * Schedule class representing a collection of class sessions.
 */
@Document(collection = "schedules")
public class Schedule {
    @Id
    private String id;
    @Field("academicPeriodId")
    private String academicPeriodId;
    @Field("classesIds")
    private List<String> classesIds = new ArrayList<>();

    @Getter
    private transient ArrayList<ClassSession> classes = new ArrayList<>();

    public Schedule() {}

    /**
     * verifies if the schedule contains a class session with the given ID.
     * @param classSession1 the ID of the class session to check.
     * @return true if the schedule contains the class session, false otherwise.
     */
    public Boolean containsClass(String classSession1) {
        return classes.stream().anyMatch(classSession -> classSession.getId().equals(classSession1));
    }
}
