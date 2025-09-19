package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.core.PetitionCommand;

public class PetitionsLoader implements PetitionObserver {

    private PetitionManager petitionManager;
    private PetitionAssistant assistant;

    public void setAssistant(PetitionAssistant assistant) {
        this.assistant = assistant;
    }

    public void setPetitionManager(PetitionManager petitionManager) {
        this.petitionManager = petitionManager;
    }

    public void loadCommandPetition(PetitionAssistant petitionAssistant, Petition petition){
        petitionAssistant.addCommand(petition.getType() , petition.toCommand(petitionManager));
    }

    public void loadCommandPetition(Petition petition){
        assistant.addCommand(petition.getType() , petition.toCommand(petitionManager));
    }

    @Override
    public void onNewPetition(Petition petition) {
        loadCommandPetition(assistant,petition);
    }
}
