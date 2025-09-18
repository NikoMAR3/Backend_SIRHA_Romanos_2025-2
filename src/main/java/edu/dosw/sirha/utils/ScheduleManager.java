package edu.dosw.sirha.utils;

import java.util.ArrayList;

public class ScheduleManager {
    private ArrayList<Schedule> schedules;

    public void addSchedule(Schedule schedule) {

    }

    public Schedule checkSchedule(String schedule) {
        return null;
    }

    public ArrayList<Schedule> checkSchedule(Student student){
        return null;
    }

    public ArrayList<Schedule> getSchedulesWithClass(ClassSession classSession1) {
        return schedules;
    }

    public Boolean checkClassInSchedule(ClassSession classSession1, Student student) {
        return false;
    }

    public ClassSession getClassInSchedule(Student student) {
        return null;
    }


}