package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a student inside the SIRHA system.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "students")

public class Student extends User{
    private TrafficLight trafficLight;
    private List<Schedule> schedules;
    private AcademicProgram academicProgram;
    private AcademicStatus academicStatus;
    private Deanery deanery;

    /**
     * Builds a new student with basic user data.
     *
     * @param id        Unique identifier of the student.
     * @param name      Full name of the student.
     * @param mail      Institutional email of the student.
     * @param document  Identity document of the student.
     */
    public Student(String id, String name, String mail, String document) {
        super(id, name, mail, document, UserType.STUDENT);
    }
}