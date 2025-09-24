package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionManager;
import edu.dosw.sirha.core.RemoveStudentPetitionCommand;

/**
 * Class representing a petition to remove a student from a class group.
 * Inherits from ClassPetition.
 */
public class RemovePetition extends ClassPetition {
    private String currentGroupId;

    /**
     * Default constructor for serialization/deserialization purposes.
     */
    public RemovePetition() {}

    /**
     * Constructor to create a RemovePetition instance.
     * @param subjectCode
     * @param observations
     * @param student
     * @param currentGroupId
     */
    public RemovePetition(String subjectCode, String observations, Student student, String currentGroupId) {
        super("REMOVE", subjectCode, observations, student);
        this.currentGroupId = currentGroupId;
    }

    /**
     * Getter and Setter for currentGroupId.
     * @return the current group ID
     */
    public String getCurrentGroupId() { return currentGroupId; }
    public void setCurrentGroupId(String currentGroupId) { this.currentGroupId = currentGroupId; }

    /**
     * Procedure to execute if the petition is accepted.
     * Here, it simply prints a message indicating the processing of the removal request.
     */
    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando solicitud de remoción del grupo: " + currentGroupId);
    }

    /**
     * Converts the petition into a command that can be executed by the PetitionManager.
     * @param manager the PetitionManager handling the command
     * @return the corresponding PetitionCommand
     */
    @Override
    public PetitionCommand toCommand(PetitionManager manager) {
        return new RemoveStudentPetitionCommand(manager, this);
    }
}
