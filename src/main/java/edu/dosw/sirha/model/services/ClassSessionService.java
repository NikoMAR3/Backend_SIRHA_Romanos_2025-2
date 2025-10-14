package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Service class for managing Class Sessions.
 * Handles business logic for class session operations including creation, modification,
 * deletion, assignment of schedules, and conflict detection for the SIRHA system.
 */
@Service
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepository;

    public ClassSessionService(ClassSessionRepository classSessionRepository) {
        this.classSessionRepository = classSessionRepository;
    }

    /**
     * Creates a new class session with validation checks.
     * Validates for conflicts before saving.
     *
     * @param session the ClassSession to create
     * @return the created ClassSession
     * @throws IllegalArgumentException if validation fails
     */
    public ClassSession createClassSession(ClassSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Class session cannot be null");
        }

        validateClassSession(session);
        validateScheduleConflicts(session);

        return classSessionRepository.save(session);
    }

    /**
     * Updates an existing class session with validation checks.
     *
     * @param session the ClassSession to update
     * @return the updated ClassSession
     * @throws IllegalArgumentException if validation fails or session doesn't exist
     */
    public ClassSession updateClassSession(ClassSession session) {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("Class session and ID cannot be null");
        }

        if (!classSessionRepository.existsById(session.getId())) {
            throw new IllegalArgumentException("Class session with ID '" + session.getId() + "' not found");
        }

        validateClassSession(session);
        validateScheduleConflictsForUpdate(session);

        return classSessionRepository.save(session);
    }

    /**
     * Deletes a class session by ID.
     *
     * @param id the ID of the class session to delete
     * @throws IllegalArgumentException if ID is null/empty or session doesn't exist
     */
    public void deleteClassSession(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        if (!classSessionRepository.existsById(id)) {
            throw new IllegalArgumentException("Class session with ID '" + id + "' not found");
        }

        classSessionRepository.deleteById(id);
    }

    /**
     * Assigns a schedule to a class session.
     *
     * @param sessionId the ID of the session
     * @param schedule the ClassSchedule to assign
     * @return the updated ClassSession
     * @throws IllegalArgumentException if validation fails
     */
    public ClassSession assignScheduleToSession(String sessionId, ClassSchedule schedule) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        if (schedule == null) {
            throw new IllegalArgumentException("Schedule cannot be null");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        validateSchedule(schedule);
        validateScheduleConflictForSession(session, schedule);

        if (session.getSchedules() == null) {
            session.setSchedules(new ArrayList<>());
        }

        session.getSchedules().add(schedule);
        return classSessionRepository.save(session);
    }

    /**
     * Searches for schedules by session ID.
     *
     * @param sessionId the ID of the session
     * @return list of ClassSchedules for the session
     */
    public List<ClassSchedule> searchSchedulesBySession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        return session.getSchedules() != null ? session.getSchedules() : new ArrayList<>();
    }

    /**
     * Searches for sessions by professor ID.
     *
     * @param professorId the ID of the professor
     * @return list of ClassSessions assigned to the professor
     */
    public List<ClassSession> searchSessionsByProfessor(String professorCode) {
        if (professorCode == null || professorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Professor code cannot be null or empty");
        }

        return classSessionRepository.findByProfessorCode(professorCode);
    }

    /**
     * Searches for sessions by subject short name.
     *
     * @param subjectShortName the short name of the subject
     * @return list of ClassSessions for the subject
     */
    public List<ClassSession> searchSessionsBySubjectShortName(String subjectShortName) {
        if (subjectShortName == null || subjectShortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name cannot be null or empty");
        }

        return classSessionRepository.findBySubjectShortName(subjectShortName);
    }

    /**
     * Checks for schedule conflicts for a given session.
     *
     * @param sessionId the ID of the session to check
     * @return true if conflicts exist, false otherwise
     */
    public boolean checkScheduleConflicts(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        return hasScheduleConflicts(session);
    }

    /**
     * Validates basic class session fields.
     */
    private void validateClassSession(ClassSession session) {
        if (session.getSubjectShortName() == null || session.getSubjectShortName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name cannot be null or empty");
        }

        if (session.getSubjectName() == null || session.getSubjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be null or empty");
        }

        /*
        if (session.getProfessorCode() == null || session.getProfessorCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Professor code cannot be null or empty");
        }
        */

        if (session.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        if (session.getEnrolledStudents() < 0) {
            throw new IllegalArgumentException("Enrolled students cannot be negative");
        }

        if (session.getEnrolledStudents() > session.getCapacity()) {
            throw new IllegalArgumentException("Enrolled students cannot exceed capacity");
        }
    }

    /**
     * Validates schedule fields.
     */
    private void validateSchedule(ClassSchedule schedule) {
        if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek().trim().isEmpty()) {
            throw new IllegalArgumentException("Day of week cannot be null or empty");
        }

        if (schedule.getStartTime() == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        if (schedule.getEndTime() == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }

        if (schedule.getClassroom() == null || schedule.getClassroom().trim().isEmpty()) {
            throw new IllegalArgumentException("Classroom cannot be null or empty");
        }

        if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    /**
     * Validates schedule conflicts for a new session.
     */
    private void validateScheduleConflicts(ClassSession session) {
        if (session.getSchedules() != null) {
            for (ClassSchedule schedule : session.getSchedules()) {
                validateScheduleConflictForSession(session, schedule);
            }
        }
    }

    /**
     * Validates schedule conflicts for session update.
     */
    private void validateScheduleConflictsForUpdate(ClassSession session) {
        if (session.getSchedules() != null) {
            for (ClassSchedule schedule : session.getSchedules()) {
                validateScheduleConflictForSessionUpdate(session, schedule);
            }
        }
    }

    /**
     * Validates conflicts for a specific schedule and session.
     */
    private void validateScheduleConflictForSession(ClassSession session, ClassSchedule schedule) {
        List<ClassSession> professorSessions = classSessionRepository.findByProfessorCode(session.getProfessorCode());
        for (ClassSession existingSession : professorSessions) {
            if (existingSession.getSchedules() != null) {
                for (ClassSchedule existingSchedule : existingSession.getSchedules()) {
                    if (hasTimeConflict(schedule, existingSchedule)) {
                        throw new IllegalArgumentException(
                            String.format("Professor %s has a time conflict on %s from %s to %s",
                                session.getProfessorCode(), schedule.getDayOfWeek(),
                                schedule.getStartTime(), schedule.getEndTime())
                        );
                    }
                }
            }
        }

        List<ClassSession> allSessions = classSessionRepository.findAll();
        for (ClassSession existingSession : allSessions) {
            if (existingSession.getSchedules() != null) {
                for (ClassSchedule existingSchedule : existingSession.getSchedules()) {
                    if (hasClassroomConflict(schedule, existingSchedule)) {
                        throw new IllegalArgumentException(
                            String.format("Classroom %s is already occupied on %s from %s to %s",
                                schedule.getClassroom(), schedule.getDayOfWeek(),
                                schedule.getStartTime(), schedule.getEndTime())
                        );
                    }
                }
            }
        }
    }

    /**
     * Validates conflicts for session update (excludes current session).
     */
    private void validateScheduleConflictForSessionUpdate(ClassSession session, ClassSchedule schedule) {

        List<ClassSession> professorSessions = classSessionRepository.findByProfessorCode(session.getProfessorCode());
        for (ClassSession existingSession : professorSessions) {
            if (!existingSession.getId().equals(session.getId()) && existingSession.getSchedules() != null) {
                for (ClassSchedule existingSchedule : existingSession.getSchedules()) {
                    if (hasTimeConflict(schedule, existingSchedule)) {
                        throw new IllegalArgumentException(
                            String.format("Professor %s has a time conflict on %s from %s to %s",
                                session.getProfessorCode(), schedule.getDayOfWeek(),
                                schedule.getStartTime(), schedule.getEndTime())
                        );
                    }
                }
            }
        }

    
        List<ClassSession> allSessions = classSessionRepository.findAll();
        for (ClassSession existingSession : allSessions) {
            if (!existingSession.getId().equals(session.getId()) && existingSession.getSchedules() != null) {
                for (ClassSchedule existingSchedule : existingSession.getSchedules()) {
                    if (hasClassroomConflict(schedule, existingSchedule)) {
                        throw new IllegalArgumentException(
                            String.format("Classroom %s is already occupied on %s from %s to %s",
                                schedule.getClassroom(), schedule.getDayOfWeek(),
                                schedule.getStartTime(), schedule.getEndTime())
                        );
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private boolean hasTimeConflict(ClassSchedule schedule1, ClassSchedule schedule2) {
        return schedule1.getDayOfWeek().equals(schedule2.getDayOfWeek()) &&
               !(schedule1.getEndTime().isBefore(schedule2.getStartTime()) || 
                 schedule1.getStartTime().isAfter(schedule2.getEndTime()));
    }

    /**
     * 
     */
    private boolean hasClassroomConflict(ClassSchedule schedule1, ClassSchedule schedule2) {
        return schedule1.getClassroom().equals(schedule2.getClassroom()) &&
               schedule1.getDayOfWeek().equals(schedule2.getDayOfWeek()) &&
               !(schedule1.getEndTime().isBefore(schedule2.getStartTime()) || 
                 schedule1.getStartTime().isAfter(schedule2.getEndTime()));
    }

    /**
     * 
     */
    private boolean hasScheduleConflicts(ClassSession session) {
        if (session.getSchedules() == null || session.getSchedules().isEmpty()) {
            return false;
        }

        try {
            validateScheduleConflictsForUpdate(session);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    

    /**/

    /**
     * Enrolls a student in a class session.
     * Validates capacity and prevents duplicate enrollments.
     *
     * @param sessionId the ID of the session
     * @param studentId the ID of the student to enroll
     * @return the updated ClassSession
     * @throws IllegalArgumentException if validation fails
     */
    public ClassSession enrollStudent(String sessionId, String studentId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));


        validateEnrollmentEligibility(session, studentId);


        if (session.getEnrolledStudentIds() == null) {
            session.setEnrolledStudentIds(new ArrayList<>());
        }
        if (session.getWaitingListStudentIds() == null) {
            session.setWaitingListStudentIds(new ArrayList<>());
        }

        if (session.getEnrolledStudentIds().contains(studentId)) {
            throw new IllegalArgumentException("Student '" + studentId + "' is already enrolled in this session");
        }

        if (session.getEnrolledStudents() >= session.getCapacity()) {
            if (!session.getWaitingListStudentIds().contains(studentId)) {
                session.getWaitingListStudentIds().add(studentId);
                return classSessionRepository.save(session);
            } else {
                throw new IllegalArgumentException("Student '" + studentId + "' is already on the waiting list");
            }
        }

        session.getEnrolledStudentIds().add(studentId);
        session.setEnrolledStudents(session.getEnrolledStudents() + 1);

        session.getWaitingListStudentIds().remove(studentId);

        return classSessionRepository.save(session);
    }

    /**
     * Withdraws a student from a class session.
     * Automatically enrolls next student from waiting list if available.
     *
     * @param sessionId the ID of the session
     * @param studentId the ID of the student to withdraw
     * @return the updated ClassSession
     * @throws IllegalArgumentException if validation fails
     */
    public ClassSession withdrawStudent(String sessionId, String studentId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        if (session.getEnrolledStudentIds() == null || !session.getEnrolledStudentIds().contains(studentId)) {
            throw new IllegalArgumentException("Student '" + studentId + "' is not enrolled in this session");
        }

        session.getEnrolledStudentIds().remove(studentId);
        session.setEnrolledStudents(session.getEnrolledStudents() - 1);

        if (session.getWaitingListStudentIds() != null && !session.getWaitingListStudentIds().isEmpty()) {
            String nextStudentId = session.getWaitingListStudentIds().remove(0);
            session.getEnrolledStudentIds().add(nextStudentId);
            session.setEnrolledStudents(session.getEnrolledStudents() + 1);
        }

        return classSessionRepository.save(session);
    }

    /**
     * Gets all sessions where a student is enrolled.
     *
     * @param studentId the ID of the student
     * @return list of ClassSessions where student is enrolled
     */
    public List<ClassSession> getStudentEnrollments(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        return classSessionRepository.findByEnrolledStudentId(studentId);
    }

    /**
     * Gets all sessions where a student is on the waiting list.
     *
     * @param studentId the ID of the student
     * @return list of ClassSessions where student is on waiting list
     */
    public List<ClassSession> getStudentWaitingList(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        return classSessionRepository.findByWaitingListStudentId(studentId);
    }

    /**
     * Checks if a student can enroll in a session.
     *
     * @param sessionId the ID of the session
     * @param studentId the ID of the student
     * @return true if student can enroll, false otherwise
     */
    public boolean canStudentEnroll(String sessionId, String studentId) {
        try {
            ClassSession session = classSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            
            validateEnrollmentEligibility(session, studentId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets enrollment statistics for a session.
     *
     * @param sessionId the ID of the session
     * @return enrollment statistics
     */
    public EnrollmentStats getEnrollmentStats(String sessionId) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        int enrolled = session.getEnrolledStudents();
        int capacity = session.getCapacity();
        int waiting = session.getWaitingListStudentIds() != null ? session.getWaitingListStudentIds().size() : 0;
        int available = Math.max(0, capacity - enrolled);

        return new EnrollmentStats(enrolled, capacity, waiting, available);
    }

    /**
     * Transfers a student from waiting list to enrolled (if capacity allows).
     *
     * @param sessionId the ID of the session
     * @param studentId the ID of the student
     * @return the updated ClassSession
     */
    public ClassSession promoteFromWaitingList(String sessionId, String studentId) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getWaitingListStudentIds() == null || !session.getWaitingListStudentIds().contains(studentId)) {
            throw new IllegalArgumentException("Student is not on waiting list");
        }

        if (session.getEnrolledStudents() >= session.getCapacity()) {
            throw new IllegalArgumentException("Session is at full capacity");
        }

        session.getWaitingListStudentIds().remove(studentId);
        if (session.getEnrolledStudentIds() == null) {
            session.setEnrolledStudentIds(new ArrayList<>());
        }
        session.getEnrolledStudentIds().add(studentId);
        session.setEnrolledStudents(session.getEnrolledStudents() + 1);

        return classSessionRepository.save(session);
    }


    /**
     * Validates if a student is eligible to enroll in a session.
     */
    private void validateEnrollmentEligibility(ClassSession session, String studentId) {
        // Verificar que no esté ya inscrito
        if (session.getEnrolledStudentIds() != null && session.getEnrolledStudentIds().contains(studentId)) {
            throw new IllegalArgumentException("Student is already enrolled");
        }

        
        List<ClassSession> studentSessions = classSessionRepository.findByEnrolledStudentId(studentId);
        for (ClassSession enrolledSession : studentSessions) {
            if (hasScheduleConflictBetweenSessions(session, enrolledSession)) {
                throw new IllegalArgumentException(
                    String.format("Schedule conflict: Student has overlapping class times between '%s' and '%s'",
                        session.getSubjectShortName(), enrolledSession.getSubjectShortName())
                );
            }
        }
    }

    /**
     * Checks if two sessions have schedule conflicts.
     */
    private boolean hasScheduleConflictBetweenSessions(ClassSession session1, ClassSession session2) {
        if (session1.getSchedules() == null || session2.getSchedules() == null) {
            return false;
        }

        for (ClassSchedule schedule1 : session1.getSchedules()) {
            for (ClassSchedule schedule2 : session2.getSchedules()) {
                if (hasTimeConflict(schedule1, schedule2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class EnrollmentStats {
        private final int enrolled;
        private final int capacity;
        private final int waitingList;
        private final int available;

        public EnrollmentStats(int enrolled, int capacity, int waitingList, int available) {
            this.enrolled = enrolled;
            this.capacity = capacity;
            this.waitingList = waitingList;
            this.available = available;
        }


        public int getEnrolled() { return enrolled; }
        public int getCapacity() { return capacity; }
        public int getWaitingList() { return waitingList; }
        public int getAvailable() { return available; }
        public double getOccupancyPercentage() { 
            return capacity > 0 ? (double) enrolled / capacity * 100 : 0; 
        }
    }

    public ClassSession searchSessionById(String id) {
        return classSessionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }

    public List<ClassSession> searchAllSessions() {
        return classSessionRepository.findAll();
}



}