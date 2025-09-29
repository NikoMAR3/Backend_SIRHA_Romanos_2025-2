package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


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

    public Student(String id, String name, String mail, String document) {
        super(id, name, mail, document, UserType.STUDENT);
    }
}