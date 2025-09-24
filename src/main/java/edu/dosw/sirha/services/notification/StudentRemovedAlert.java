package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

/**
 * Alert for when a student is removed from a class session.
 */
public class StudentRemovedAlert extends BaseAlert {
    private ClassSession classSession;
    private String studentId;

    /**
     * Constructor for StudentRemovedAlert.
     * @param classSession the class session from which the student was removed
     * @param studentId the ID of the removed student
     */
    public StudentRemovedAlert(ClassSession classSession, String studentId) {
        super("Estudiante " + studentId + " removido del grupo " + classSession.getId(), "STUDENT_REMOVED", "INFO");
        this.classSession = classSession;
        this.studentId = studentId;
    }
}