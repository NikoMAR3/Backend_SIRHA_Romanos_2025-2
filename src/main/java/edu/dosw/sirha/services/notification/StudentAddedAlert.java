package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Student;

public class StudentAddedAlert extends BaseAlert {
    private ClassSession classSession;
    private Student student;

    public StudentAddedAlert(ClassSession classSession, Student student) {
        super("Estudiante " + student.getId() + " agregado al grupo " + classSession.getId(), "STUDENT_ADDED", "INFO");
        this.classSession = classSession;
        this.student = student;
    }
}