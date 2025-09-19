package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Petition;

public interface PetitionObserver {
    public void onNewPetition( Petition petition);
}
