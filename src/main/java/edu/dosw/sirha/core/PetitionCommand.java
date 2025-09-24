package edu.dosw.sirha.core;

import edu.dosw.sirha.model.Petition;

/**
 * Interface for petition commands implementing the Command pattern.
 * It defines methods for executing and undoing a petition action.
 */
public interface PetitionCommand {
    void execute();
    void undo();
    Petition getPetition();
}
