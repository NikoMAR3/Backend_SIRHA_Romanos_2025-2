package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionType;
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
        return petition.getType() == PetitionType.REMOVE_SUBJECT; // ESTO TOCA VERLO BIEN
    }
}