package edu.dosw.sirha.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Class representing an academic subject with prerequisites and credits.
 */
@Getter
@Setter
@NoArgsConstructor
public class Subject {
    private String name;
    private String code;
    private List<Subject> preRequisites = new ArrayList<>();
    private int credits;

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
