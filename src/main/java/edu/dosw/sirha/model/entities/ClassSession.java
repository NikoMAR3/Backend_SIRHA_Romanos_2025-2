package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @DBRef
    private Professor professor;

    @DBRef
    private List<ClassSchedule> schedules;
}
