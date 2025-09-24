package edu.dosw.sirha.services;

import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.model.TrafficLight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages traffic lights for students.
 */
public class TrafficLightManager {
    private HashMap<Student, TrafficLight> trafficLights;

    /**
     * Initializes the TrafficLightManager with an empty traffic light map.
     */
    public TrafficLightManager() {
        trafficLights = new HashMap<>();
    }

    /**
     * Returns the map of student traffic lights.
     * @return the map of student traffic lights
     */
    public HashMap<Student, TrafficLight> getTrafficLights() {return trafficLights;}

    /**
     * Adds a traffic light for a student.
     * @param student the student
     * @return
     */
    public TrafficLight getTrafficLight(Student student){
        return trafficLights.get(student);
    }

    /**
     * Adds a traffic light for a student.
     * @return the traffic light added
     */
    public TrafficLight checkTrafficLight(){
        return new TrafficLight();
    }
}