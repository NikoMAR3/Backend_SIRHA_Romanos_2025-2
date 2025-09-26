package edu.dosw.sirha.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

/**
 * Class representing an academic subject with prerequisites and credits.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "subjects")
public class Subject {
    @Id
    private String id;
    @Field("classSessionsIds")
    private List<String> classSessionsIds = new ArrayList<>();
    @Field("name")
    private String name;
    @Field("code")
    private String code;
    @Field("preRequisitesIds")
    private List<String> preRequisitesIds = new ArrayList<>();
    @Field("credits")
    private int credits;
    @Field("level")
    private int level;

    private transient List<Subject> preRequisites = new ArrayList<>();

    /**
     * Parameterized constructor to initialize a subject.
     *
     * @param name         the name of the subject
     * @param code         the unique code of the subject
     * @param preRequisites the list of prerequisite subjects
     * @param credits      the number of credits for the subject
     */
    public Subject(String name, String code, List<Subject> preRequisites, int credits) {
        this.name = name;
        this.code = code;
        if (preRequisites != null) this.preRequisites = preRequisites;
        this.credits = credits;
    }
}
