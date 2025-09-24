package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class Dean {
    private String dean_id;
    private String major;

    private final ScheduleManager scheduleManager;
    private final PetitionManager petitionManager;
    private final TrafficLightManager trafficLightManager;
    private final ClassManager classManager;
    private final PetitionAssistant assistant;

    @Autowired
    public Dean(ScheduleManager scheduleManager,
                PetitionManager petitionManager,
                TrafficLightManager trafficLightManager,
                ClassManager classManager,
                PetitionAssistant assistant) {
        this.scheduleManager = scheduleManager;
        this.petitionManager = petitionManager;
        this.trafficLightManager = trafficLightManager;
        this.classManager = classManager;
        this.assistant = assistant;
    }

    public void configure(String dean_id, String major) {
        this.dean_id = dean_id;
        this.major = major;
    }

    public String getDean_id() {
        return dean_id;
    }

    public String getMajor() {
        return major;
    }

    public Schedule checkSchedule(Student student){
        return scheduleManager.checkSchedule(student);
    }

    public ArrayList<Petition> checkPetitions(Student student){
        return assistant.getStudentPetitions(student.getId());
    }

    public ArrayList<Petition> checkPetitions(String type){
        return assistant.getPetitionsByType(type);
    }

    public void answerPetition(Petition petition, Boolean approve){
        assistant.answerPetition(petition, approve);
    }

    public TrafficLight checkTrafficLight(){
        return trafficLightManager.checkTrafficLight();
    }

    public TrafficLight checkTrafficLight(Student student){
        return trafficLightManager.getTrafficLight(student);
    }

    public void modifyQuota(ClassSession classSession, int newQuota){
        classManager.modifyClassQuota(classSession, newQuota);
    }

    public Integer checkQuota(ClassSession classSessions){
        return classManager.checkClassQuota(classSessions);
    }
}