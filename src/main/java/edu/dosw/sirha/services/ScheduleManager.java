package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Schedule;
import edu.dosw.sirha.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScheduleManager {
    private HashMap<Student, Schedule> schedules;

    public ScheduleManager() {
        schedules = new HashMap<>();
    }


    public HashMap<Student, Schedule> getSchedules() { return schedules;}

    public void addSchedule(Student student, Schedule schedule) {
        schedules.put(student, schedule);
    }

    public Schedule checkSchedule(Student student) {
        return schedules.get(student);
    }

    public Boolean checkClassInSchedule(String classSession1, Student student) {
        return schedules.get(student).containsClass(classSession1);
    }

    public ArrayList<ClassSession> getClassInSchedule(Student student) {
        return schedules.get(student).getClasses();
    }


}