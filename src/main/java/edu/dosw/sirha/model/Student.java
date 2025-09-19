package edu.dosw.sirha.model;

import edu.dosw.sirha.services.PetitionObserver;

import java.util.*;

public class Student {
    private String name;
    private String id;
    private List<String> programs = new ArrayList<>();
    private Schedule schedule = new Schedule();
    private List<Petition> petitions = new ArrayList<>();
    private ArrayList<PetitionObserver> observers = new ArrayList<>();

    public Student(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Student() {}

    public String getName() {return name;}
    public void setName(String name) { this.name = name; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<String> getPrograms() { return programs; }
    public void addProgram(String program) { this.programs.add(program); }
    public Schedule getSchedule() { return schedule; }
    public List<Petition> getPetitions() {
        return petitions;
    }

    public void addPetition(Petition petition) {
        if (petition != null) this.petitions.add(petition);
    }

    public void addToClass(ClassSession cls) {
        Objects.requireNonNull(cls);
        this.schedule.getClasses().add(cls);
        cls.addStudent(this);
    }

    public void removeFromClass(ClassSession cls) {
        this.schedule.getClasses().remove(cls);
        cls.delStudent(this.id);
    }

    //falta lo de notificar observers
}