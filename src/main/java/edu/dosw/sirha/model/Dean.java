package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Represents a Dean in the academic system.
 * The Dean has the ability to manage schedules, petitions, traffic lights, and class quotas.
 */
@Component
@Getter
@Setter
public class Dean {
    private String dean_id;
    private String major;

    private final ScheduleManager scheduleManager;
    private final PetitionManager petitionManager;
    private final TrafficLightManager trafficLightManager;
    private final ClassManager classManager;
    private final PetitionAssistant assistant;

    /**
     * Constructor for the Dean class.
     * @param scheduleManager
     * @param petitionManager
     * @param trafficLightManager
     * @param classManager
     * @param assistant
     */
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

    /**
     * Configures the Dean with an ID and major.
     * @param dean_id
     * @param major
     */
    public void configure(String dean_id, String major) {
        this.dean_id = dean_id;
        this.major = major;
    }

    /**
     * Checks the schedule of a given student.
     * @param student
     * @return The schedule of the student.
     */
    public Schedule checkSchedule(Student student){
        return scheduleManager.checkSchedule(student);
    }

    /**
     * Submits a petition on behalf of a student.
     * @param student
     * @return The submitted petition.
     */
    public ArrayList<Petition> checkPetitions(Student student){
        return assistant.getStudentPetitions(student.getId());
    }

    /**
     * Checks petitions of a specific type.
     * @param type
     * @return List of petitions of the specified type.
     */
    public ArrayList<Petition> checkPetitions(String type){
        return assistant.getPetitionsByType(type);
    }

    /**
     * Answers a petition with approval or rejection.
     * @param petition
     * @param approve
     */
    public void answerPetition(Petition petition, Boolean approve){
        assistant.answerPetition(petition, approve);
    }

    /**
     * Checks the overall traffic light status.
     * @return The current traffic light status.
     */
    public TrafficLight checkTrafficLight(){
        return trafficLightManager.checkTrafficLight();
    }

    /**
     * Checks the traffic light status of a specific student.
     * @param student
     * @return The traffic light status of the student.
     */
    public TrafficLight checkTrafficLight(Student student){
        return trafficLightManager.getTrafficLight(student);
    }

    /**
     * Modifies the quota of a specific class session.
     * @param classSession
     * @param newQuota
     */
    public void modifyQuota(ClassSession classSession, int newQuota){
        classManager.modifyClassQuota(classSession, newQuota);
    }

    /**
     * Checks the quota of a specific class session.
     * @param classSessions
     * @return The current quota of the class session.
     */
    public Integer checkQuota(ClassSession classSessions){
        return classManager.checkClassQuota(classSessions);
    }
}
