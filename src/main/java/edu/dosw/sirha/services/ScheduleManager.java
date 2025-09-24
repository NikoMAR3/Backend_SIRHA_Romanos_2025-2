package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Schedule;
import edu.dosw.sirha.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages student schedules.
 */
public class ScheduleManager {
    private HashMap<Student, Schedule> schedules;

    /**
     * Initializes the ScheduleManager with an empty schedule map.
     */
    public ScheduleManager() {
        schedules = new HashMap<>();
    }


    /**
     * Returns the map of student schedules.
     * @return the map of student schedules
     */
    public HashMap<Student, Schedule> getSchedules() { return schedules;}

    /**
     * Adds a schedule for a student.
     * @param student the student
     * @param schedule the schedule to add
     */
    public void addSchedule(Student student, Schedule schedule) {
        schedules.put(student, schedule);
    }

    /**
     * Checks and returns the schedule for a student.
     * @param student the student
     * @return the student's schedule
     */
    public Schedule checkSchedule(Student student) {
        return schedules.get(student);
    }

    /**
     * Checks if a class session is in a student's schedule.
     * @param classSession1 the class session to check
     * @param student the student
     * @return true if the class session is in the student's schedule, false otherwise
     */
    public Boolean checkClassInSchedule(String classSession1, Student student) {
        return schedules.get(student).containsClass(classSession1);
    }

    /**
     * Returns the list of class sessions in a student's schedule.
     * @param student the student
     * @return the list of class sessions in the student's schedule
     */
    public ArrayList<ClassSession> getClassInSchedule(Student student) {
        return schedules.get(student).getClasses();
    }


}