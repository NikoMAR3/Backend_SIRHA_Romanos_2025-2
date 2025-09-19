package edu.dosw.sirha.core;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.services.PetitionManager;



public class ChangePetitionCommand implements  PetitionCommand{

    private PetitionManager manager;
    private ChangePetition petition;

    public ChangePetitionCommand(PetitionManager manager, ChangePetition petition){
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {
        petition.getClass();
        manager.makeChange(petition);
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
