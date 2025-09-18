package edu.dosw.sirha.utils;

public class RemoveStudentCommand implements Command {
    private PetitionManager manager;
    private RemovePetition petition;
    private boolean executed = false;

    public RemoveStudentCommand(PetitionManager manager, RemovePetition petition) {
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
}
