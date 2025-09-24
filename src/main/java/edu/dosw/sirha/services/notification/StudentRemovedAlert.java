package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;

public class StudentRemovedAlert extends BaseAlert {
    private ClassSession classSession;
    private String studentId;

    public StudentRemovedAlert(ClassSession classSession, String studentId) {
        super("Estudiante " + studentId + " removido del grupo " + classSession.getId(), "STUDENT_REMOVED", "INFO");
        this.classSession = classSession;
        this.studentId = studentId;
    }
}