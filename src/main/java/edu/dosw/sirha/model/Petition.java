package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionManager;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract Class representing a petition made by a student.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class Petition {
    private String id = UUID.randomUUID().toString();
    private PetitionType type;
    private String subjectCode;
    private String observations;
    private String priority;
    private LocalDateTime dateOfCreation = LocalDateTime.now();
    private PetitionStatus status = PetitionStatus.PENDIENTE;
    private Student student;

    /**
     * Constructor to create a Petition instance.
     * @param type the type of petition (e.g., "ADD", "CHANGE")
     * @param subjectCode the code of the subject
     * @param observations any observations related to the petition
     * @param student the student making the petition
     */
    public Petition(PetitionType type, String subjectCode, String observations, Student student) {
        this.type = type;
        this.subjectCode = subjectCode;
        this.observations = observations;
        this.student = student;
    }

    /**
     * Gets the student ID from the associated student.
     * @return the student ID
     */
    public String getStudentId() {
        return student.getId();
    }

    public abstract void ifAcceptedProcedure();

    public abstract PetitionCommand toCommand(PetitionManager manager);
}
