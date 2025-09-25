package edu.dosw.sirha.services;

import edu.dosw.sirha.model.*;

import java.util.*;

/**
 * Manages class sessions, including adding/removing students and modifying quotas.
 */
public class ClassService {
    private HashMap<String, ClassSession> classSessions;

    /**
     * Initializes the ClassService with an empty set of class sessions.
     */
    public ClassService() {
        classSessions = new HashMap<>();
    }

    /**
     * Retrieves all class sessions managed by this ClassService.
     * @return HashMap containing all class sessions with their IDs as keys
     */
    public HashMap<String, ClassSession> getClassSessions() {return classSessions;}

    /**
     * Retrieves a specific class session by its ID (case-insensitive).
     * @param class1 the ID of the class session to retrieve
     * @return the ClassSession object associated with the given ID, or null if not found
     */
    public ClassSession getClass(String class1){
        return classSessions.get(class1.toLowerCase());
    }

    /**
     * Modifies the maximum quota for a specific class session.
     * @param classSession1 the ID of the class session to modify
     * @param newQuota the new quota value to set
     */
    public void modifyMaxClassQuota(String classSession1, int newQuota){
        classSessions.get(classSession1).setCurrentQuota(newQuota);
    }

    /**
     * Checks the maximum quota for a specific class session.
     * @param classSession1 the ID of the class session to check
     * @return the current quota of the specified class session
     */
    public int checkMaxClassQuota(String classSession1){
        return classSessions.get(classSession1).getCurrentQuota();
    }

    /**
     * Checks the current quota for a given class session object.
     * @param classSession1 the ClassSession object to check
     * @return the current quota of the specified class session
     */
    public int checkClassQuota(ClassSession classSession1){
        return classSessions.get(classSession1.getId()).getCurrentQuota();
    }

    /**
     * Modifies the quota for a specific class session using the ClassSession object.
     * @param classSession the ClassSession object whose quota will be modified
     * @param newQuota the new quota value to set
     */
    public void modifyClassQuota(ClassSession classSession, int newQuota){
        classSessions.get(classSession.getId()).setCurrentQuota(newQuota);
    }

    /**
     * Retrieves a class session by its ID (case-insensitive).
     * @param classId the ID of the class session to retrieve
     * @return the ClassSession object associated with the given ID, or null if not found
     */
    public ClassSession getClassById(String classId) {
        return classSessions.get(classId.toLowerCase());
    }

    /**
     * Adds a student to a specific group/class session.
     * @param student the Student object to add to the group
     * @param groupId the ID of the group/class session where the student will be added
     */
    public void addStudentToGroup(Student student, String groupId){
        classSessions.get(groupId).addStudent(student);
    }

    /**
     * Removes a student from a specific group/class session.
     * @param student the Student object to remove from the group
     * @param targetGroupId the ID of the group/class session from which the student will be removed
     */
    public void removeStudentFromGroup(Student student, String targetGroupId) {
        classSessions.get(targetGroupId).getStudents().remove(student);
    }
}
