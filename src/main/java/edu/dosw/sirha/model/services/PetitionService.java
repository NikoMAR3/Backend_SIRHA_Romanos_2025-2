package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.components.util.PetitionHandler;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.persistence.repository.PetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetitionService {
    //private final PetitionRepository petitionRepository;
    private final PetitionHandler petitionHandlerChain;

    //public Petition createPetition(Petition petition) {
        //Petition saved = petitionRepository.save(petition);
        //petitionHandlerChain.answerPetition(saved);
        //return saved;
    //}
}
