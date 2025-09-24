package edu.dosw.sirha.services;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.model.Petition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages petitions and their associated commands.
 */
public class PetitionAssistant{

    private HashMap<String, PetitionCommand> petitionCommands = new HashMap<>();

    /**
     * Adds a petition command to the assistant.
     * @param typeOfCommand
     * @param command
     */
    public void addCommand(String typeOfCommand,PetitionCommand command){
        petitionCommands.put(typeOfCommand,command);
    }

    /**
     * Retrieves all petition commands.
     * @return HashMap containing all petition commands with their types as keys
     */
    public HashMap<String, PetitionCommand> getPetitions(){
        return petitionCommands;
    }

    /**
     * Retrieves all petitions submitted by a specific student.
     * @param studentId the ID of the student whose petitions are to be retrieved
     * @return ArrayList of Petition objects submitted by the specified student
     */
    public ArrayList<Petition> getStudentPetitions(String studentId) {
        return petitionCommands.values().stream()
                .map(PetitionCommand::getPetition)
                .filter(p -> p.getStudentId().equals(studentId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Retrieves all petitions of a specific type.
     * @param type the type of petitions to be retrieved
     * @return ArrayList of Petition objects of the specified type
     */
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

    /**
     * Answers a petition by either approving or rejecting it.
     * @param petition the petition to be answered
     * @param approve true to approve the petition, false to reject it
     * @throws IllegalArgumentException if there is no command associated with the petition
     */
    public void answerPetition(Petition petition, boolean approve) {
        PetitionCommand command = getCommandByPetition(petition);
        if (command == null) {
            throw new IllegalArgumentException("No existe un comando para esta petición");
        }
        if (approve) {
            command.execute();
        } else {
            command.undo();
        }
    }


    /**
     * Retrieves petition commands by their type.
     * @param typePetition the type of petitions to be retrieved
     * @return
     */
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

    /**
     * Retrieves the command associated with a specific petition.
     * @param petition the petition whose command is to be retrieved
     * @return the PetitionCommand associated with the given petition, or null if not found
     */
    public PetitionCommand getCommandByPetition(Petition petition) {
        return petitionCommands.values().stream()
                .filter(cmd -> cmd.getPetition().getId().equals(petition.getId()))
                .findFirst()
                .orElse(null);
    }

}
