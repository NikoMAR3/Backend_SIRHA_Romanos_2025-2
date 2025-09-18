package edu.dosw.sirha.core;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.services.PetitionManager;

public class ChangeGroupCommand implements Command {
    private PetitionManager manager;
    private ChangePetition petition;
    private boolean executed = false;

    public ChangeGroupCommand(PetitionManager manager, ChangePetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {
        if (!executed) {
            manager.removeStudentFromGroup(petition.getStudentId(), petition.getCurrentGroupId());
            manager.addStudentToGroup(petition.getStudentId(), petition.getTargetGroupId());
            executed = true;
        }
    }

    @Override
    public void undo() {
        if (executed) {
            manager.removeStudentFromGroup(petition.getStudentId(), petition.getTargetGroupId());
            manager.addStudentToGroup(petition.getStudentId(), petition.getCurrentGroupId());
            executed = false;
        }
    }
}
