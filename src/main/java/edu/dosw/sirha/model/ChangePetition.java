package edu.dosw.sirha.model;

import edu.dosw.sirha.core.ChangeGroupPetitionCommand;
import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionService;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representing a petition to change a student's group in a subject.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangePetition extends ClassPetition {
    private String currentGroupId;
    private String targetGroupId;


    /**
     * Constructor to create a ChangePetition instance.
     * @param subjectCode the code of the subject
     * @param observations any observations related to the petition
     * @param student the student requesting the change
     * @param currentGroupId the current group ID of the student
     * @param targetGroupId the target group ID to which the student wants to move
     */
    public ChangePetition(String subjectCode, String observations, Student student,
                          String currentGroupId, String targetGroupId) {
        super("CHANGE", subjectCode, observations, student);
        this.currentGroupId = currentGroupId;
        this.targetGroupId = targetGroupId;
    }

    /**
     * Procedure to execute if the petition is accepted.
     * Here, it simply prints a message indicating the processing of the group change.
     */
    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando cambio de grupo " + currentGroupId + " a " + targetGroupId);
    }

    /**
     * Converts the petition into a command that can be executed by the PetitionService.
     * @param manager the PetitionService handling the command
     * @return the corresponding PetitionCommand
     */
    @Override
    public PetitionCommand toCommand(PetitionService manager) {
        return new ChangeGroupPetitionCommand(manager, this);
    }
}
