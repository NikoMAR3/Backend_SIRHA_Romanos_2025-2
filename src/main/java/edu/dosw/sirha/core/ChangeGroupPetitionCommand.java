package edu.dosw.sirha.core;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.services.PetitionService;


/**
 * Command to change a group petition.
 */
public class ChangeGroupPetitionCommand implements  PetitionCommand{

    private PetitionService manager;
    private ChangePetition petition;

    /**
     * Constructs a ChangeGroupPetitionCommand.
     *
     * @param manager  the PetitionService to handle the petition
     * @param petition the ChangePetition containing change details
     */
    public ChangeGroupPetitionCommand(PetitionService manager, ChangePetition petition){
        this.manager = manager;
        this.petition = petition;
    }

    /**
     * Executes the command to change the group as per the petition.
     * Sets the petition status to "APPROVED".
     */
    @Override
    public void execute() {
        manager.makeChange(petition);
        petition.setStatus("APPROVED");
    }

    /**
     * Undoes the command by reverting the group change.
     * Sets the petition status to "DENIED".
     */
    @Override
    public void undo() {
        petition.setStatus("DENIED");
    }

    /**
     * Returns the petition associated with this command.
     *
     * @return the ChangePetition
     */
    @Override
    public Petition getPetition() {
        return petition;
    }
}
