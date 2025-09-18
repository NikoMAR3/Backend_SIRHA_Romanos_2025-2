package edu.dosw.sirha.Backend_SIRHA_Romanos_2025_2.util;


public class Dean {
    private String dean_id;
    private String major;
    private ScheduleManager scheduleManager;
    private PetitionManager petitionManager;
    private TrafficLightManager trafficLightManager;
    private ClassManager classManager;
    private PetitionCommand petitionCommand;


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

    }

    public ArrayList<Petition> checkPetitions(String type){

    }

    public void answerPetition(Petition petition){

    }

    public TrafficLight checkTrafficLight(){

    }

    public TrafficLight checkTrafficLight(Student student){

    }

    public void modifyQuota(Class class){

    }

    public Integer checkQuota(Class class){

    }


}