package edu.dosw.sirha.core;

import edu.dosw.sirha.model.Petition;

public interface PetitionCommand {
    void execute();
    void undo();
    Petition getPetitionOfCommand();
}
