package edu.dosw.sirha.utils;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Petition {
    private String id = UUID.randomUUID().toString();
    private String type;
    private String subjectCode;
    private String observations;
    private String studentId;
    private String priority;
    private LocalDateTime dateOfCreation = LocalDateTime.now();
    private String status = "PENDIENTE";

    public Petition() {}

    public Petition(String type, String subjectCode, String observations, String studentId) {
        this.type = type;
        this.subjectCode = subjectCode;
        this.observations = observations;
        this.studentId = studentId;
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
    public String getStudentId() {
        return studentId;
    }

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

    public abstract toCommand(PetitionManager manager);
}