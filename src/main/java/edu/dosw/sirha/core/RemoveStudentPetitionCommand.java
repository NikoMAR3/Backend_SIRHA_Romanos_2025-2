package edu.dosw.sirha.core;

import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.model.RemovePetition;
import edu.dosw.sirha.services.PetitionManager;

public class RemoveStudentPetitionCommand implements PetitionCommand {
    private PetitionManager manager;
    private RemovePetition petition;

    public RemoveStudentPetitionCommand(PetitionManager manager, RemovePetition petition) {
        this.manager = manager;
        this.petition = petition;
    }

    @Override
    public void execute() {

    }

    @Override
    public void undo() {

    }

    @Override
    public Petition getPetitionOfCommand() {
        return petition;
    }
}
