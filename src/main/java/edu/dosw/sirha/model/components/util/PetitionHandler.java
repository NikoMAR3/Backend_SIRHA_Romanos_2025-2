package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.model.entities.Petition;
import lombok.Setter;

/**
 * Abstract base class for handling petitions in a chain of responsibility pattern.
 */
 public abstract class PetitionHandler {
    protected Petition petition;
    @Setter
    protected PetitionHandler nextHandler;

    /**
     * Attempts to answer the given petition.
     * Concrete implementations decide whether to proces the petition or delegate it to the next handler.
     * @param petition the petition to evaluate
     * @return true if the petition is approved, false otherwise
     */
    public abstract boolean answerPetition(Petition petition);
}
