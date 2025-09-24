package edu.dosw.sirha.model;

public abstract class ClassPetition extends Petition {

    public ClassPetition() {}

    public ClassPetition(String type, String subjectCode, String observations, Student student) {
        super(type, subjectCode, observations, student);
    }
}
