package edu.dosw.sirha.core;

import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.model.RemovePetition;
import edu.dosw.sirha.services.PetitionManager;

public class RemoveStudentPetitionCommand implements PetitionCommand {
    private PetitionManager manager;
    private RemovePetition petition;
    private boolean executed = false;

    public RemoveStudentPetitionCommand(PetitionManager manager, RemovePetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {
        if (!executed) {
            manager.removeStudentFromGroup(petition.getStudentId(), petition.getCurrentGroupId());
            executed = true;
        }
    }

    @Override
    public void undo() {
        if (executed) {
            manager.addStudentToGroup(petition.getStudentId(), petition.getCurrentGroupId());
            executed = false;
        }
    }

    @Override
    public Petition getPetitionOfCommand() {
        return petition;
    }
}
