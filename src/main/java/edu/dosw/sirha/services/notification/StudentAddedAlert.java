package edu.dosw.sirha.services.notification;

import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Student;

/**
 * StudentAddedAlert class representing an alert for a student being added to a class session.
 * Inherits from BaseAlert and sets the type to "STUDENT_ADDED".
 */
public class StudentAddedAlert extends BaseAlert {
    private ClassSession classSession;
    private Student student;

    /**
     * Constructor for StudentAddedAlert.
     *
     * @param classSession The class session to which the student was added.
     * @param student      The student who was added.
     */
    public StudentAddedAlert(ClassSession classSession, Student student) {
        super("Estudiante " + student.getId() + " agregado al grupo " + classSession.getId(), "STUDENT_ADDED", "INFO");
        this.classSession = classSession;
        this.student = student;
    }
}