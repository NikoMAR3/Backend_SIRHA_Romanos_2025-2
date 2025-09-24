package edu.dosw.sirha.core;

import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.model.RemovePetition;
import edu.dosw.sirha.services.PetitionManager;

/**
 * Command to remove a student petition.
 * Implements the PetitionCommand interface.
 */
public class RemoveStudentPetitionCommand implements PetitionCommand {
    private PetitionManager manager;
    private RemovePetition petition;
    private boolean executed = false;

    /**
     * Constructs a RemoveStudentPetitionCommand.
     *
     * @param manager  the PetitionManager to handle the petition
     * @param petition the RemovePetition containing student and target group information
     */
    public RemoveStudentPetitionCommand(PetitionManager manager, RemovePetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    /**
     * Executes the command to remove the student from the current group.
     * If the command has already been executed, it will not execute again.
     */
    @Override
    public void execute() {
        if (!executed) {
            manager.removeStudentFromGroup(petition.getStudent(), petition.getCurrentGroupId());
            executed = true;
        }
    }

    /**
     * Undoes the command by adding the student back to the current group.
     */
    @Override
    public void undo() {
        if (executed) {
            manager.addStudentToGroup(petition.getStudent(), petition.getCurrentGroupId());
            executed = false;
        }
    }

    /**
     * Returns the petition associated with this command.
     *
     * @return the RemovePetition
     */
    @Override
    public Petition getPetition() {
        return petition;
    }
}
