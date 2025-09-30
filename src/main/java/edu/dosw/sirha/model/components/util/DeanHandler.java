package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import org.springframework.stereotype.Component;

@Component
public class DeanHandler extends PetitionHandler {
    @Override
    public boolean answerPetition(Petition petition) {
        if (canAnswer(petition)) {
            return true;
        } else if (nextHandler != null) {
            return nextHandler.answerPetition(petition);
        }
        return false;
    }

    private boolean canAnswer(Petition petition) {
        return petition.getPriority() == PetitionPriority.URGENT; // ESTO TOCA VERLO BIEN
    }
}