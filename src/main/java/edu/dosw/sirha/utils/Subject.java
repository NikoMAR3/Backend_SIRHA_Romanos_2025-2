package edu.dosw.sirha.utils;

import java.util.*;

public class Subject {
    private String name;
    private String code;
    private List<Subject> preRequisites = new ArrayList<>();
    private int credits;

    public Subject(String name, String code, List<Subject> preRequisites, int credits) {
        this.name = name;
        this.code = code;
        if (preRequisites != null) this.preRequisites = preRequisites;
        this.credits = credits;
    }

    public Subject() {}

    public String getName(){ return name; }
    public String getCode(){ return code; }

    public List<Subject> getPreRequisites(){ return preRequisites; }
    public int getCredits(){ return credits; }
}