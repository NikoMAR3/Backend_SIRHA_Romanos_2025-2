package edu.dosw.sirha.core;

import edu.dosw.sirha.model.AddPetition;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.services.PetitionManager;

/**
 * Command to add a student to a group based on a petition.
 */

public class AddStudentPetitionCommand implements PetitionCommand {
    private PetitionManager manager;
    private AddPetition petition;
    private boolean executed = false;

    /**
     * Constructs an AddStudentPetitionCommand.
     *
     * @param manager  the PetitionManager to handle the petition
     * @param petition the AddPetition containing student and target group information
     */
    public AddStudentPetitionCommand(PetitionManager manager, AddPetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    /**
     * Executes the command to add the student to the target group.
     * If the command has already been executed, it will not execute again.
     */
    @Override
    public void execute() {
        if (!executed) {
            manager.addStudentToGroup(petition.getStudent(), petition.getTargetGroupId());
            executed = true;
        }
    }

    /**
     * Undoes the command by removing the student from the target group.
     */
    @Override
    public void undo() {
        if (executed) {
            manager.removeStudentFromGroup(petition.getStudent(), petition.getTargetGroupId());
            executed = false;
        }
    }

    /**
     * Returns the petition associated with this command.
     *
     * @return the AddPetition
     */
    @Override
    public Petition getPetition() {
        return petition;
    }
}
