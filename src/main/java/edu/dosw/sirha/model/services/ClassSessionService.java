package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Class Sessions.
 * Handles business logic for class session operations including creation, modification,
 * deletion, assignment of schedules, and conflict detection for the SIRHA system.
 */
@Service
public class ClassSessionService {

    private ClassSessionRepository classSessionRepository;
    public ClassSessionService(ClassSessionRepository classSessionRepository) {
        this.classSessionRepository = classSessionRepository;
    }

    /**
     * Creates a new class session.
     * Validates that the session doesn't already exist and saves it to the database.
     * 
     * @param session the ClassSession to be created
     * @return the created ClassSession with assigned ID
     * @throws IllegalArgumentException if session is null or has validation errors
     */
    public ClassSession createClassSession(ClassSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Class session cannot be null");
        }
        if (session.getSubjectShortName() == null || session.getSubjectShortName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name is required");
        }
        if (session.getProfessorId() == null || session.getProfessorId().trim().isEmpty()) {
            throw new IllegalArgumentException("Professor ID is required");
        }
        if (session.getCapacity() <= 0) {
            throw new IllegalArgumentException("Session capacity must be greater than 0");
        }
        if (session.getEnrolledStudents() < 0) {
            throw new IllegalArgumentException("Enrolled students cannot be negative");
        }
        if (session.getEnrolledStudents() > session.getCapacity()) {
            throw new IllegalArgumentException("Enrolled students cannot exceed capacity");
        }
        
        return classSessionRepository.save(session);
    }

    /**
     * Updates an existing class session.
     * Validates that the session exists and updates its information.
     * 
     * @param session the ClassSession with updated information
     * @return the updated ClassSession
     * @throws IllegalArgumentException if session is null or doesn't exist
     */
    public ClassSession updateClassSession(ClassSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Class session cannot be null");
        }
        if (session.getId() == null || !classSessionRepository.existsById(session.getId())) {
            throw new IllegalArgumentException("Class session with ID '" + session.getId() + "' does not exist");
        }
        if (session.getEnrolledStudents() > session.getCapacity()) {
            throw new IllegalArgumentException("Enrolled students (" + session.getEnrolledStudents() + 
                                             ") cannot exceed capacity (" + session.getCapacity() + ")");
        }
        
        return classSessionRepository.save(session);
    }

    /**
     * Deletes a class session by its ID.
     * Validates that the session exists before deletion.
     * 
     * @param sessionId the unique identifier of the class session to delete
     * @throws IllegalArgumentException if ID is null or session doesn't exist
     */
    public void deleteClassSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        if (!classSessionRepository.existsById(sessionId)) {
            throw new IllegalArgumentException("Class session with ID '" + sessionId + "' does not exist");
        }
        
        classSessionRepository.deleteById(sessionId);
    }

    /**
     * Assigns a schedule to a class session.
     * This method would typically validate schedule conflicts and update the session.
     * 
     * @param sessionId the ID of the class session
     * @param schedule the ClassSchedule to assign
     * @return the updated ClassSchedule
     * @throws IllegalArgumentException if parameters are invalid or conflicts exist
     */
    public ClassSchedule assignScheduleToSession(String sessionId, ClassSchedule schedule) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule cannot be null");
        }
        
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));
        
        if (schedule.getDayOfWeek() != null && schedule.getStartTime() != null && 
            schedule.getEndTime() != null && schedule.getClassroom() != null) {
            
            boolean hasConflict = checkScheduleConflicts(sessionId);
            if (hasConflict) {
                throw new IllegalArgumentException("Schedule conflict detected for session '" + sessionId + "'");
            }
        }
        
        return schedule;
    }

    /**
     * Searches for schedules by session ID.
     * Retrieves all schedules associated with a specific class session.
     * 
     * @param sessionId the ID of the class session
     * @return a list of ClassSchedules for the session
     * @throws IllegalArgumentException if sessionId is null or session doesn't exist
     */
    public List<ClassSchedule> searchSchedulesBySession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));
        
        return session.getSchedules() != null ? session.getSchedules() : List.of();
    }

    /**
     * Searches for class sessions by professor ID.
     * 
     * @param professorId the ID of the professor
     * @return a list of ClassSessions assigned to the professor
     * @throws IllegalArgumentException if professorId is null or empty
     */
    public List<ClassSession> searchSessionsByProfessor(String professorId) {
        if (professorId == null || professorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Professor ID cannot be null or empty");
        }
        
        return classSessionRepository.findByProfessorId(professorId);
    }

    /**
     * Searches for class sessions by subject short name.
     * 
     * @param subjectShortName the short name of the subject
     * @return a list of ClassSessions for the subject
     * @throws IllegalArgumentException if subjectShortName is null or empty
     */
    public List<ClassSession> searchSessionsBySubjectShortName(String subjectShortName) {
        if (subjectShortName == null || subjectShortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name cannot be null or empty");
        }
        
        return classSessionRepository.findBySubjectShortName(subjectShortName);
    }


    //FALTA IMPLEMENTACION
    /**
     * Checks for schedule conflicts for a given session.
     * Uses the repository's conflict detection method.
     * 
     * @param sessionId the ID of the session to check
     * @return true if there are conflicts, false otherwise
     * @throws IllegalArgumentException if sessionId is null or empty
     */
    public boolean checkScheduleConflicts(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        

        try {
            return classSessionRepository.existsScheduleConflicts(
                sessionId, "MONDAY", "08:00", "10:00", "A101", "prof123"
            );
        } catch (Exception e) {
            return false;
        }
    }
    

}