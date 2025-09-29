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


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedules")

/**
 * Represents the schedule of a student
 */
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
    private int semester;

    @Field
    private int credits;

    @DBRef
    private ClassSession classSession;

    @DBRef
    private Professor professor;

}
