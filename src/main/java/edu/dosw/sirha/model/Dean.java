package edu.dosw.sirha.model;


import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.*;


import java.util.ArrayList;
import java.util.stream.Collectors;

public class Dean {
    private String dean_id;
    private String major;
    private ScheduleManager scheduleManager;
    private PetitionManager petitionManager;
    private TrafficLightManager trafficLightManager;
    private ClassManager classManager;
    private PetitionAssistant assistant;


    public Dean(String dean_id, String major) {
        this.dean_id = dean_id;
        this.major = major;
    }

    public String getDean_id() {
        return dean_id;
    }

    public Schedule checkSchedule(Student student){
        return scheduleManager.checkSchedule(student);
    }

    public ArrayList<Petition> checkPetitions(Student student){
        return assistant.getPetitions().values().stream()
                .map(PetitionCommand::getPetitionOfCommand)
                .filter(p -> p.getStudentId().equals(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Petition> checkPetitions(String type){
        return assistant.getPetitions(type).values().stream()
                .map(PetitionCommand::getPetitionOfCommand)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void answerPetition(Petition petition,Boolean approve){
        PetitionCommand command = assistant.getCommandByPetition(petition);
        if (command == null) {
            throw new IllegalArgumentException("No existe un comando para esta petición");
        }

        if (approve) {
            command.execute();     // aprueba → corre el flujo normal
        } else {
            command.undo();        // rechaza → deshace/descarta
        }
    }

    public TrafficLight checkTrafficLight(){
        return trafficLightManager.checkTrafficLight();
    }

    public TrafficLight checkTrafficLight(Student student){
        return trafficLightManager.getTrafficLight(student);
    }

    public void modifyQuota(ClassSession classSession){
        return classManager.modifyClassQuota();
    }

    public Integer checkQuota(ClassSession classSessions){
        return classManager.checkClassQuota();
    }


}