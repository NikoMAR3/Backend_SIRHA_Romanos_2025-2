package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionService;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract Class representing a petition made by a student.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "petitions")
public abstract class Petition {
    @Id
    private String id = UUID.randomUUID().toString();
    @Field("type")
    private String type;
    @Field("subjectCode")
    private String subjectCode;
    @Field("observations")
    private String observations;
    @Field("studentId")
    private String studentId;
    @Field("priority")
    private String priority;
    @Field("dateOfCreation")
    private LocalDateTime dateOfCreation = LocalDateTime.now();
    @Field("status")
    private String status;
    @Field("targetGroupId")
    private String targetGroupId;
    @Field("currentGroupId")
    private String currentGroupId;

    private transient PetitionType petitionType;
    private transient PetitionStatus petitionStatus;
    private transient Student student;

    /**
     * Constructor to create a Petition instance.
     * @param type the type of petition (e.g., "ADD", "CHANGE")
     * @param subjectCode the code of the subject
     * @param observations any observations related to the petition
     * @param student the student making the petition
     */
    public Petition(String type, String subjectCode, String observations, Student student) {
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

    public abstract PetitionCommand toCommand(PetitionService manager);
}
