package edu.dosw.sirha.model;

import edu.dosw.sirha.services.PetitionObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * Represents a student with personal details, enrolled programs, schedule, and petitions.
 * Implements the observer pattern to notify observers about new petitions.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private String fullName;
    private String id;
    private String email;
    private String career;
    private int currentSemester;
    private TrafficLight trafficLight = new TrafficLight();;
    private List<String> programs = new ArrayList<>();
    private Schedule schedule = new Schedule();
    private ArrayList<Petition> petitions = new ArrayList<>();
    private ArrayList<PetitionObserver> observers = new ArrayList<>();

    /**
     * Constructor to initialize a student with name and ID.
     * @param fullName
     * @param id
     */
    public Student(String fullName, String id) {
        this.fullName = fullName;
        this.id = id;
    }

    /**
     * Adds a program to the student's list of enrolled programs.
     * @param program the program to add
     */
    public void addProgram(String program) {
        this.programs.add(program);
    }

    /**
     * Adds a petition to the student's list of petitions and notifies observers.
     * @param petition the petition to add
     */
    public void addPetition(Petition petition) {
        if (petition != null) this.petitions.add(petition);
        notifyPetitionObserver(petition);
    }

    /**
     * Adds the student to a class session and updates both the student's schedule and the class's student list.
     * @param cls
     */
    public void addToClass(ClassSession cls) {
        Objects.requireNonNull(cls);
        this.schedule.getClasses().add(cls);
        cls.addStudent(this);
    }

    /**
     * Removes the student from a class session and updates both the student's schedule and the class's student list.
     * @param cls
     */
    public void removeFromClass(ClassSession cls) {
        this.schedule.getClasses().remove(cls);
        cls.delStudent(this.id);
    }

    /**
     * Adds an observer to the list of petition observers.
     * @param loader
     */
    public void addObserver(PetitionObserver loader) {
        observers.add(loader);
    }

    /**
     * Notifies all observers about a new petition.
     * @param petition
     */
    public void notifyPetitionObserver(Petition petition){
        observers.forEach(petitionObserver1 -> petitionObserver1.onNewPetition(petition));
    }
}
