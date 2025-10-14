package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Entity that epresents an academic subject.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subjects")
public class Subject {
    @Id
    private String id;

    private String shortName;
    private String name;

    @DBRef
    private List<Subject> prerequisites;

    private int credits;
    private int level;

    @DBRef
    private List<ClassSession> classSessions;

    @DBRef
    private String programId;
}
