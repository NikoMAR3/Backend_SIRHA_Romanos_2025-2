package edu.dosw.sirha.core;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.services.PetitionManager;

public class ChangeGroupPetitionCommand implements PetitionCommand {
    private PetitionManager manager;
    private ChangePetition petition;

    public ChangeGroupPetitionCommand(PetitionManager manager, ChangePetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {
        manager.removeStudentFromGroup(petition.getStudentId(), petition.getCurrentGroupId());
        manager.addStudentToGroup(petition.getStudentId(), petition.getTargetGroupId());
    }

    @Override
    public void undo() {
            manager.removeStudentFromGroup(petition.getStudentId(), petition.getTargetGroupId());
            manager.addStudentToGroup(petition.getStudentId(), petition.getCurrentGroupId());
    }

    @Override
    public Petition getPetitionOfCommand() {
        return petition;
    }
}
