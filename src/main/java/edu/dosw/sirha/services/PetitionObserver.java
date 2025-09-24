package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Petition;

/**
 * PetitionObserver interface for observing new petitions.
 */
public interface PetitionObserver {

    /**
     * Called when a new petition is created.
     * @param petition the new petition
     */
    public void onNewPetition( Petition petition);
}
