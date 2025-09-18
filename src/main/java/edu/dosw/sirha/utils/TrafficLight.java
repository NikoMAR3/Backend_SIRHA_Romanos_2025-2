package edu.dosw.sirha.utils;

import java.util.ArrayList;

public class TrafficLight {
    private String major;
    private ArrayList<Subject> subjectsAproved;
    private ArrayList<Subject> subjectsCanceled;
    private ArrayList<Subject> subjectsFailed;
    private ArrayList<Subject> subjects;

    public TrafficLight(String major) {

    }

    public String getMajor() {
        return major;
    }

    public ArrayList<Subject> getSubjects() {
        return subjects;
    }
}