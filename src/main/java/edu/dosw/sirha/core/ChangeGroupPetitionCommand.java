package edu.dosw.sirha.core;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.services.PetitionManager;



public class ChangeGroupPetitionCommand implements  PetitionCommand{

    private PetitionManager manager;
    private ChangePetition petition;

    public ChangeGroupPetitionCommand(PetitionManager manager, ChangePetition petition){
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {
        manager.makeChange(petition);
        petition.setStatus("APPROVED");
    }

    @Override
    public void undo() {
        petition.setStatus("DENIED");
    }

    @Override
    public Petition getPetitionOfCommand() {
        return petition;
    }
}
