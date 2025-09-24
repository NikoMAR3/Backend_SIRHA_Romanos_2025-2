package edu.dosw.sirha.model;

import edu.dosw.sirha.core.AddStudentPetitionCommand;
import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionManager;

/**
 * Class representing a petition to add a student to a specific group.
 */
public class AddPetition extends ClassPetition {
    private String targetGroupId;

    /**
     *  Default constructor for serialization/deserialization purposes.
     */
    public AddPetition() {}

    /**
     * Constructor to create an AddPetition instance.
     * @param subjectCode
     * @param observations
     * @param student
     * @param targetGroupId
     */
    public AddPetition(String subjectCode, String observations, Student student, String targetGroupId) {
        super("ADD", subjectCode, observations, student);
        this.targetGroupId = targetGroupId;
    }

    /**
     * Getter and Setter for targetGroupId.
     * @return the target group ID
     */
    public String getTargetGroupId() { return targetGroupId; }
    public void setTargetGroupId(String targetGroupId) { this.targetGroupId = targetGroupId; }

    /**
     * Procedure to execute if the petition is accepted.
     * Here, it simply prints a message indicating the processing of the add request.
     */
    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando solicitud de adición a grupo: " + targetGroupId);
    }

    /**
     * Converts the petition into a command that can be executed by the PetitionManager.
     * @param manager the PetitionManager handling the command
     * @return the corresponding PetitionCommand
     */
    @Override
    public PetitionCommand toCommand(PetitionManager manager) {
        return new AddStudentPetitionCommand(manager, this);
    }
}
