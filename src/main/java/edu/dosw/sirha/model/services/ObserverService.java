package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Observer service for monitoring class session capacity and generating alerts.
 * Monitors class sessions to detect when they reach 90% capacity or are full.
 */
@Service
public class ObserverService {

    private static final Logger logger = LoggerFactory.getLogger(ObserverService.class);
    private static final double WARNING_THRESHOLD = 0.9;

    private final ClassSessionRepository classSessionRepository;

    public ObserverService(ClassSessionRepository classSessionRepository) {
        this.classSessionRepository = classSessionRepository;
    }

    /**
     * Monitors the load of a specific class session and generates alerts based on capacity.
     * Checks if the session has reached 90% capacity (warning) or is completely full (critical alert).
     *
     * @param sessionId the unique identifier of the class session to monitor
     * @throws IllegalArgumentException if sessionId is null, empty, or session doesn't exist
     */
    public void monitorLoadClassSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        if (session.getCapacity() <= 0) {
            logger.warn("Class session '{}' has invalid capacity: {}", sessionId, session.getCapacity());
            return;
        }

        double loadPercentage = (double) session.getEnrolledStudents() / session.getCapacity();

        if (loadPercentage >= 1.0) {
            generateFullCapacityAlert(session);
        } else if (loadPercentage >= WARNING_THRESHOLD) {
            generateHighCapacityWarning(session, loadPercentage);
        }
    }

    /**
     * Monitors all class sessions and generates alerts for those reaching capacity thresholds.
     * Iterates through all sessions and checks their capacity status.
     */
    public void monitorAllClassSessions() {
        try {
            List<ClassSession> allSessions = classSessionRepository.findAll();

            if (allSessions.isEmpty()) {
                logger.info("No class sessions found to monitor");
                return;
            }

            for (ClassSession session : allSessions) {
                if (session.getCapacity() > 0) {
                    double loadPercentage = (double) session.getEnrolledStudents() / session.getCapacity();

                    if (loadPercentage >= 1.0) {
                        generateFullCapacityAlert(session);
                    } else if (loadPercentage >= WARNING_THRESHOLD) {
                        generateHighCapacityWarning(session, loadPercentage);
                    }
                }
            }

            logger.info("Monitoring completed for {} class sessions", allSessions.size());

        } catch (Exception e) {
            logger.error("Error occurred while monitoring all class sessions: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates a warning alert when a class session reaches 90% capacity.
     *
     * @param session the ClassSession that reached the warning threshold
     * @param loadPercentage the current load percentage of the session
     */
    private void generateHighCapacityWarning(ClassSession session, double loadPercentage) {
        String message = String.format(
                "WARNING: Class session '%s' (%s) is at %.1f%% capacity (%d/%d students). " +
                        "Consider monitoring enrollment closely.",
                session.getSubjectShortName(),
                session.getSubjectName(),
                loadPercentage * 100,
                session.getEnrolledStudents(),
                session.getCapacity()
        );

        logger.warn("CAPACITY WARNING - Session ID: {} - {}", session.getId(), message);

    }

    /**
     * Generates a critical alert when a class session reaches full capacity.
     *
     * @param session the ClassSession that is completely full
     */
    private void generateFullCapacityAlert(ClassSession session) {
        String message = String.format(
                "CRITICAL: Class session '%s' (%s) is FULL (%d/%d students). " +
                        "No more enrollments can be accepted.",
                session.getSubjectShortName(),
                session.getSubjectName(),
                session.getEnrolledStudents(),
                session.getCapacity()
        );

        logger.error("FULL CAPACITY ALERT - Session ID: {} - {}", session.getId(), message);

    }

    /**
     * Checks if a specific class session has reached the warning threshold (90% capacity).
     *
     * @param sessionId the unique identifier of the class session
     * @return true if the session is at or above 90% capacity, false otherwise
     * @throws IllegalArgumentException if sessionId is null, empty, or session doesn't exist
     */
    public boolean isSessionAtWarningCapacity(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        if (session.getCapacity() <= 0) {
            return false;
        }

        double loadPercentage = (double) session.getEnrolledStudents() / session.getCapacity();
        return loadPercentage >= WARNING_THRESHOLD;
    }

    /**
     * Checks if a specific class session is at full capacity.
     *
     * @param sessionId the unique identifier of the class session
     * @return true if the session is completely full, false otherwise
     * @throws IllegalArgumentException if sessionId is null, empty, or session doesn't exist
     */
    public boolean isSessionFull(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session with ID '" + sessionId + "' not found"));

        return session.getEnrolledStudents() >= session.getCapacity();
    }
}
