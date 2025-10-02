package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for processing petitions at the dean level.
 */
 @Component
public class DeanHandler extends PetitionHandler {
    /**
     * Attempts to answer the petition. If the dean cannot process it, the request is forwarded to the next handler.
     * @param petition the petition to be evaluated
     * @return true if the petition is approved, false otherwise
     */
    @Override
    public boolean answerPetition(Petition petition) {
        if (canAnswer(petition)) {
            return true;
        } else if (nextHandler != null) {
            return nextHandler.answerPetition(petition);
        }
        return false;
    }

    /**
     * Checks if the dean can process the given petition.
     *
     * @param petition the petition to evaluate
     * @return true if the petition has "URGENT" priority.
     */
    private boolean canAnswer(Petition petition) {
        return petition.getPriority() == PetitionPriority.URGENT;
    }
}