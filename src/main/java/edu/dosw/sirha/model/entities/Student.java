package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Student entity class representing a student in the system that Inherits from User.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "students")
public class Student extends User{

    @DBRef
    private TrafficLight trafficLight;

    @DBRef
    private List<Schedule> schedules;

    @DBRef
    private AcademicProgram academicProgram;

    @DBRef
    private AcademicStatus academicStatus;

    @DBRef
    private Deanery deanery;

    /**
     * Constructor for creating a Student with basic user information.
     *
     * @param id       the unique identifier of the student
     * @param name     the name of the student
     * @param mail     the email address of the student
     * @param document the document number of the student
     */
    public Student(String id, String name, String mail, String document) {
        super(id, name, mail, document, UserType.STUDENT);
    }
}