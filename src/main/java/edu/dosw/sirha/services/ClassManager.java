package edu.dosw.sirha.services;

import edu.dosw.sirha.model.*;

import java.util.*;

public class ClassManager {
    private HashMap<String, ClassSession> classSessions;

    public ClassManager() {
        classSessions = new HashMap<>();
    }

    public HashMap<String, ClassSession> getClassSessions() {return classSessions;}

    public ClassSession getClass(String class1){
        return classSessions.get(class1.toLowerCase());
    }

    public void modifyMaxClassQuota(String classSession1, int newQuota){
        classSessions.get(classSession1).setCurrentQuota(newQuota);
    }

    public int checkMaxClassQuota(String classSession1){
        return classSessions.get(classSession1).getCurrentQuota();
    }

    public int checkClassQuota(ClassSession classSession1){
        return classSessions.get(classSession1.getId()).getCurrentQuota();
    }


    public void modifyClassQuota(ClassSession classSession, int newQuota){
        classSessions.get(classSession.getId()).setCurrentQuota(newQuota);
    }

    public ClassSession getClassById(String classId) {
        return classSessions.get(classId.toLowerCase());
    }


    public void addStudentToGroup(Student student, String groupId){
        classSessions.get(groupId).addStudent(student);
    }

    public void removeStudentFromGroup(Student student, String targetGroupId) {
        classSessions.get(targetGroupId).getStudents().remove(student);
    }
}