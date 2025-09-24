package edu.dosw.sirha.services;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.model.Petition;

import java.util.ArrayList;
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

    public ArrayList<Petition> getStudentPetitions(String studentId) {
        return petitionCommands.values().stream()
                .map(PetitionCommand::getPetition)
                .filter(p -> p.getStudentId().equals(studentId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Petition> getPetitionsByType(String type) {
        ArrayList<Petition> result = new ArrayList<>();
        for (Map.Entry<String, PetitionCommand> entry : petitionCommands.entrySet()) {
            PetitionCommand command = entry.getValue();
            Petition petition = command.getPetition();
            if (petition != null && petition.getType().equals(type)) {
                result.add(petition);
            }
        }
        return result;
    }

    public void answerPetition(Petition petition, boolean approve) {
        PetitionCommand command = getCommandByPetition(petition);
        if (command == null) {
            throw new IllegalArgumentException("No existe un comando para esta petición");
        }
        if (approve) {
            command.execute();     // aprueba → corre el flujo normal
        } else {
            command.undo();        // rechaza → deshace/descarta
        }
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
                .filter(cmd -> cmd.getPetition().getId().equals(petition.getId()))
                .findFirst()
                .orElse(null);
    }

}
