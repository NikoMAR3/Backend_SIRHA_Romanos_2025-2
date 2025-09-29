package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


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
    private List<Subject> prerequisites;
    private int credits;
    private int level;
    private ClassSession classSession;
}
