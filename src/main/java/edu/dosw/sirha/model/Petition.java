package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionManager;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Petition {
    private String id = UUID.randomUUID().toString();
    private String type;
    private String subjectCode;
    private String observations;
    private String priority;
    private LocalDateTime dateOfCreation = LocalDateTime.now();
    private String status = "PENDIENTE";
    private Student student;

    public Petition() {}

    public Petition(String type, String subjectCode, String observations, Student student) {
        this.type = type;
        this.subjectCode = subjectCode;
        this.observations = observations;
        this.student = student;
    }

    public String getId() {
        return id;
    }
    public String getType() {
        return type;
    }
    public String getSubjectCode() {
        return subjectCode;
    }
    public String getObservations() {
        return observations;
    }
    public Student getStudent() {
        return student;
    }
    public String getStudentId() { return student.getId(); }

    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public LocalDateTime getDateOfCreation() {
        return dateOfCreation;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public abstract void ifAcceptedProcedure();

    public abstract PetitionCommand toCommand(PetitionManager manager);
}