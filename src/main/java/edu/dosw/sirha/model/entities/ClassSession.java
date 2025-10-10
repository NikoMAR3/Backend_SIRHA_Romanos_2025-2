package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Entity representing a class session.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "classSessions")
public class ClassSession {
    @Id
    private String id;
    
    @Field
    private String subjectShortName;
    
    @Field
    private String subjectName;
    
    @Field
    private String professorId;
    
    @Field
    private int capacity;
    
    @Field
    private int enrolledStudents;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @DBRef
    private Professor professor;

    private List<ClassSchedule> schedules;

    @Field
    private List<String> enrolledStudentIds;
    
    @Field
    private List<String> waitingListStudentIds;
    

}
