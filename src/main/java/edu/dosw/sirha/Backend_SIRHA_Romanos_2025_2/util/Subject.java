package edu.dosw.sirha.Backend_SIRHA_Romanos_2025_2.util;

import java.util.ArrayList;

public class Subject {
    private String name;
    private String code;
    private ArrayList<Subject> preRequisites;
    private int credits;

    public Subject(String name, String code, ArrayList<Subject> preRequisites, int credits) {
        this.name = name;
        this.code = code;
        this.preRequisites = preRequisites;
        this.credits = credits;
    }
    public String getName(){
        return name;
    }

    public String getCode(){
        return code;
    }

    public ArrayList<Subject> getPreRequisites(){return preRequisites;}
    public int getCredits(){return credits;}



}