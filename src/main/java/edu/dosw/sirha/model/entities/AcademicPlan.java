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
 * Represents an academic plan within an academic program.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "academic_plans")
public class AcademicPlan {
    @Id
    private String id;

    @Field
    private String name;

    @DBRef
    private AcademicProgram program;

    @DBRef
    private TrafficLight trafficLight;

    @DBRef
    private List<Subject> subjects;
}
