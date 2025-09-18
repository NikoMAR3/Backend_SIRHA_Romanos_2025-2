package edu.dosw.sirha.utils;

public class AddStudentCommand implements Command {
    private PetitionManager manager;
    private AddPetition petition;
    private boolean executed = false;

    public AddStudentCommand(PetitionManager manager, AddPetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {
        if (!executed) {
            manager.addStudentToGroup(petition.getStudentId(), petition.getTargetGroupId());
            executed = true;
        }
    }

    @Override
    public void undo() {
        if (executed) {
            manager.removeStudentFromGroup(petition.getStudentId(), petition.getTargetGroupId());
            executed = false;
        }
    }
}
