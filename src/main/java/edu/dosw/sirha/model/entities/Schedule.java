package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Entity that represents the schedule of a student
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedules")
public class Schedule {

    @Id
    private String id;

    @Field
    private String studentId;

    @Field
    private String subjectShortName;

    @Field
    private String name;

    @Field
    private String classroom;

    @Field
    private String dayOfWeek;

    @Field
    private LocalDateTime startTime;

    @Field
    private LocalDateTime endTime;

    @Field
    private String semester;

    @Field
    private int credits;

    @DBRef
    private List<ClassSession> classSessions;

    private List<Subject> subjects;

    @DBRef
    private Professor professor;

    private String program;
}
