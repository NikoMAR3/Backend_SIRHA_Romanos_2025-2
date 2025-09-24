package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.core.PetitionCommand;

/**
 * PetitionsLoader is responsible for loading petitions into the PetitionAssistant.
 * It implements the PetitionObserver interface to react to new petitions.
 */
public class PetitionsLoader implements PetitionObserver {
    private PetitionManager petitionManager;
    private PetitionAssistant assistant;

    /**
     * Sets the PetitionAssistant instance to be used by this PetitionsLoader.
     * @param assistant the PetitionAssistant instance
     */
    public void setAssistant(PetitionAssistant assistant) {
        this.assistant = assistant;
    }

    /**
     * Sets the PetitionManager instance to be used by this PetitionsLoader.
     * @param petitionManager the PetitionManager instance
     */
    public void setPetitionManager(PetitionManager petitionManager) {
        this.petitionManager = petitionManager;
    }

    /**
     * Loads a petition into the given PetitionAssistant as a command.
     * @param petitionAssistant the PetitionAssistant to load the command into
     * @param petition the petition to be loaded
     */
    public void loadCommandPetition(PetitionAssistant petitionAssistant, Petition petition){
        petitionAssistant.addCommand(petition.getType() , petition.toCommand(petitionManager));
    }

    /**
     * Loads a petition into the internal PetitionAssistant as a command.
     * @param petition the petition to be loaded
     */
    public void loadCommandPetition(Petition petition){
        assistant.addCommand(petition.getType() , petition.toCommand(petitionManager));
    }

    /**
     * Called when a new petition is created.
     * @param petition the new petition
     */
    @Override
    public void onNewPetition(Petition petition) {
        loadCommandPetition(assistant,petition);
    }
}
