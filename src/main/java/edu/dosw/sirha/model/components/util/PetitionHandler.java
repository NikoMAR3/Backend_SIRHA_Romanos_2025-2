package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.model.entities.Petition;
import lombok.Setter;

public abstract class PetitionHandler {
    protected Petition petition;
    @Setter
    protected PetitionHandler nextHandler;

    public abstract boolean answerPetition(Petition petition);
}
