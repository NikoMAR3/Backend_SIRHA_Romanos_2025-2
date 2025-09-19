package edu.dosw.sirha.services;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.model.Petition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PetitionAssistant{

    private HashMap<String, PetitionCommand> petitionCommands = new HashMap<>();

    public void addCommand(String typeOfCommand,PetitionCommand command){
        petitionCommands.put(typeOfCommand,command);
    }

    public HashMap<String, PetitionCommand> getPetitions(){
        return petitionCommands;
    }

    public HashMap<String, PetitionCommand> getPetitions(String typePetition) {
        return petitionCommands.entrySet().stream()
                .filter(entry -> entry.getKey().equals(typePetition))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        HashMap::new
                ));
    }
    public PetitionCommand getCommandByPetition(Petition petition) {
        return petitionCommands.values().stream()
                .filter(cmd -> cmd.getPetitionOfCommand().getId().equals(petition.getId()))
                .findFirst()
                .orElse(null);
    }

}
