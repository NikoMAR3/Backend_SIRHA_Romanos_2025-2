package edu.dosw.sirha.model;

import edu.dosw.sirha.core.ChangeGroupCommand;
import edu.dosw.sirha.core.Command;
import edu.dosw.sirha.services.PetitionManager;

public class ChangePetition extends ClassPetition {
    private String currentGroupId;
    private String targetGroupId;

    public ChangePetition() {}

    public ChangePetition(String subjectCode, String observations, String studentId,
                          String currentGroupId, String targetGroupId) {
        super("CHANGE", subjectCode, observations, studentId);
        this.currentGroupId = currentGroupId;
        this.targetGroupId = targetGroupId;
    }

    public String getCurrentGroupId() { return currentGroupId; }
    public void setCurrentGroupId(String currentGroupId) { this.currentGroupId = currentGroupId; }

    public String getTargetGroupId() { return targetGroupId; }
    public void setTargetGroupId(String targetGroupId) { this.targetGroupId = targetGroupId; }

    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando cambio de grupo " + currentGroupId + " a " + targetGroupId);
    }

    @Override
    public Command toCommand(PetitionManager manager) {
        return new ChangeGroupCommand(manager, this);
    }
}
