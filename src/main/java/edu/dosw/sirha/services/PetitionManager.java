
package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.model.Student;

/**
 * PetitionManager is responsible for handling petitions related to class sessions.
 * It validates and processes petitions such as changing, adding, or removing students from classes.
 */
public class PetitionManager {
    private ClassManager classManager;

    /**
     * Sets the ClassManager instance to be used by this PetitionManager.
     * @param classManager the ClassManager instance
     */
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    /**
     * Validates if a petition can be processed based on its type.
     * @param petition the petition to be validated
     * @return true if the petition can be processed, false otherwise
     */
    public Boolean validateIfPossible(Petition petition) {
        switch (petition.getType()) {
            case "CHANGE":
                return validateChange((ChangePetition) petition);
            case "ADD":
                // validateAdd((AddPetition) petition);
                return true;
            case "REMOVE":
                // validateRemove((RemovePetition) petition);
                return true;
            default:
                return false;
        }
    }

    /**
     * Validates if a change petition can be processed.
     * @param petition the change petition to be validated
     * @return true if the change can be processed, false otherwise
     */
    public boolean validateChange(ChangePetition petition) {
        ClassSession currentGroup = classManager.getClassById(petition.getCurrentGroupId());
        ClassSession targetGroup = classManager.getClassById(petition.getTargetGroupId());

        boolean isEnrolled = currentGroup.getStudents().stream()
                .anyMatch(s -> s.getId().equals(petition.getStudentId()));
        boolean hasQuota = targetGroup.hasAvailableQuota();

        return isEnrolled && hasQuota;
    }

    /**
     * Processes a change petition if it is valid.
     * @param petition the change petition to be processed
     * @throws IllegalStateException if the student is not in the current group
     */
    public void makeChange(ChangePetition petition) {
        if(validateChange(petition)) {
            ClassSession currentGroup = classManager.getClassById(petition.getCurrentGroupId());
            ClassSession targetGroup = classManager.getClassById(petition.getTargetGroupId());
            Student student = currentGroup.getStudents().stream()
                    .filter(s -> s.getId().equals(petition.getStudentId())) // filtra
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("El estudiante no está en el grupo actual"));;

            student.removeFromClass(currentGroup);
            student.addToClass(targetGroup);
        }
    }

    /**
     * Adds a student to a group.
     * @param student the student to be added
     * @param groupId the ID of the group to which the student will be added
     */
    public void addStudentToGroup(Student student, String groupId){
        classManager.addStudentToGroup(student, groupId);
    }

    /**
     * Removes a student from a group.
     * @param student the student to be removed
     * @param targetGroupId the ID of the group from which the student will be removed
     */
    public void removeStudentFromGroup(Student student, String targetGroupId) {
        classManager.removeStudentFromGroup(student, targetGroupId);
    }
}
