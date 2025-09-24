package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.model.TrafficLight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrafficLightManager {
    private HashMap<Student, TrafficLight> trafficLights;

    public TrafficLightManager() {
        trafficLights = new HashMap<>();
    }
    public HashMap<Student, TrafficLight> getTrafficLights() {return trafficLights;}
    public TrafficLight getTrafficLight(Student student){
        return trafficLights.get(student);
    }

    public TrafficLight checkTrafficLight(){
        return new TrafficLight();
    }

}