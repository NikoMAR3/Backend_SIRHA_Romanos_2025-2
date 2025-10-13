package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.persistence.repository.ScheduleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Schedule entities.
 * Provides business logic for CRUD operations and schedule-related queries
 * in the academic management system.
 */
@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Creates a new schedule in the system.
     * Validates schedule data and checks for conflicts before creation.
     *
     * @param schedule the schedule entity to create
     * @return the created schedule with generated ID
     * @throws IllegalArgumentException if schedule data is invalid
     * @throws RuntimeException if schedule conflicts exist or creation fails
     */
    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule cannot be null");
        }

        if (schedule.getStudentId() == null || schedule.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        try {
            return scheduleRepository.save(schedule);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create schedule", e);
        }
    }

    /**
     * Deletes a schedule by its ID.
     * Performs validation before deletion.
     *
     * @param id the unique identifier of the schedule to delete
     * @return true if schedule was deleted, false if not found
     * @throws IllegalArgumentException if ID is null or empty
     * @throws RuntimeException if deletion fails
     */
    @Transactional
    public boolean deleteSchedule(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Schedule ID cannot be null or empty");
        }

        try {
            if (!scheduleRepository.existsById(id)) {
                return false;
            }

            scheduleRepository.deleteById(id);
            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete schedule", e);
        }
    }

    /**
     * Updates an existing schedule.
     * Modifies schedule information while preserving the ID.
     *
     * @param schedule the schedule entity with updated information
     * @return the updated schedule
     * @throws IllegalArgumentException if schedule data is invalid
     * @throws RuntimeException if schedule not found or update fails
     */
    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        if (schedule == null || schedule.getId() == null) {
            throw new IllegalArgumentException("Schedule or schedule ID cannot be null");
        }

        try {
            Schedule existingSchedule = scheduleRepository.findById(schedule.getId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + schedule.getId()));

            if (schedule.getStudentId() != null && !schedule.getStudentId().trim().isEmpty()) {
                existingSchedule.setStudentId(schedule.getStudentId().trim());
            }

            if (schedule.getSubjectShortName() != null && !schedule.getSubjectShortName().trim().isEmpty()) {
                existingSchedule.setSubjectShortName(schedule.getSubjectShortName().trim());
            }

            if (schedule.getName() != null && !schedule.getName().trim().isEmpty()) {
                existingSchedule.setName(schedule.getName().trim());
            }

            if (schedule.getClassroom() != null && !schedule.getClassroom().trim().isEmpty()) {
                existingSchedule.setClassroom(schedule.getClassroom().trim());
            }

            if (schedule.getDayOfWeek() != null && !schedule.getDayOfWeek().trim().isEmpty()) {
                existingSchedule.setDayOfWeek(schedule.getDayOfWeek().trim());
            }

            if ((schedule.getSemester() != null && schedule.getSemester().matches("\\d{4}-[12]"))) {
                existingSchedule.setSemester(schedule.getSemester());
            }

            if (schedule.getCredits() > 0) {
                existingSchedule.setCredits(schedule.getCredits());
            }

            if (schedule.getStartTime() != null) {
                existingSchedule.setStartTime(schedule.getStartTime());
            }

            if (schedule.getEndTime() != null) {
                existingSchedule.setEndTime(schedule.getEndTime());
            }

            if (schedule.getClassSessions() != null && !schedule.getClassSessions().isEmpty()) {
                existingSchedule.setClassSessions(schedule.getClassSessions());
            }

            if (schedule.getProfessor() != null) {
                existingSchedule.setProfessor(schedule.getProfessor());
            }

            return scheduleRepository.save(existingSchedule);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update schedule", e);
        }
    }


    /**
     * Searches for a schedule by student ID.
     * Returns the current active schedule for the student.
     *
     * @param studentId the unique identifier of the student
     * @return the student's schedule
     * @throws IllegalArgumentException if student ID is null or empty
     * @throws RuntimeException if schedule not found or search fails
     */
    public Schedule searchScheduleByStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        try {
            return scheduleRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found for student ID: " + studentId));

        } catch (Exception e) {
            throw new RuntimeException("Failed to search schedule by student ID", e);
        }
    }

    /**
     * Retrieves all schedules in the system.
     *
     * @return a list of all schedules
     * @throws RuntimeException if retrieval fails
     */
    public List<Schedule> searchAllSchedules() {
        try {
            return scheduleRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedules", e);
        }
    }

    /**
     * Searches for all schedules containing a specific subject by its ID.
     *
     * @param subjectId the unique identifier of the subject
     * @return a list of schedules containing the specified subject
     * @throws IllegalArgumentException if subject ID is invalid
     * @throws RuntimeException if search fails
     */
    public List<Schedule> searchScheduleBySubject(String subjectId) {
        try {
            return scheduleRepository.findBySubjectId(subjectId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search schedules by subject", e);
        }
    }

    /**
     * Searches for all schedules containing a subject with the specified short name.
     *
     * @param shortName the short name or code of the subject
     * @return a list of schedules containing subjects with the specified short name
     * @throws IllegalArgumentException if short name is null or empty
     * @throws RuntimeException if search fails
     */
    public List<Schedule> searchScheduleBySubjectShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name cannot be null or empty");
        }

        try {
            return scheduleRepository.findBySubjectShortName(shortName.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search schedules by subject short name", e);
        }
    }

    /**
     * Searches for all schedules containing a subject with the specified name.
     *
     * @param name the full name of the subject
     * @return a list of schedules containing subjects with the specified name
     * @throws IllegalArgumentException if name is null or empty
     * @throws RuntimeException if search fails
     */
    public List<Schedule> searchScheduleByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be null or empty");
        }

        try {
            return scheduleRepository.findBySubjectName(name.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search schedules by subject name", e);
        }
    }

    /**
     * Searches for all schedules for a specific semester.
     *
     * @param semester the semester identifier
     * @return a list of schedules for the specified semester
     * @throws IllegalArgumentException if semester is null or empty
     * @throws RuntimeException if search fails
     */
    public List<Schedule> searchByScheduleBySemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Semester cannot be null or empty");
        }

        try {
            return scheduleRepository.findBySemester(semester.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search schedules by semester", e);
        }
    }

    /**
     * Searches for all schedules for a specific academic program.
     *
     * @param program the academic program identifier
     * @return a list of schedules for the specified program
     * @throws IllegalArgumentException if program is null or empty
     * @throws RuntimeException if search fails
     */
    public List<Schedule> searchByScheduleByProgram(String program) {
        if (program == null || program.trim().isEmpty()) {
            throw new IllegalArgumentException("Program cannot be null or empty");
        }

        try {
            return scheduleRepository.findByProgram(program.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search schedules by program", e);
        }
    }

    /**
     * Checks if a new schedule creates conflicts with existing schedules for a student.
     * Validates time overlaps and capacity constraints.
     *
     * @param studentId the unique identifier of the student
     * @param newSchedule the new schedule to validate
     * @return true if conflicts exist, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     */
    public boolean checkScheduleConflicts(int studentId, Schedule newSchedule) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be greater than zero");
        }
        if (newSchedule == null) {
            throw new IllegalArgumentException("New schedule cannot be null");
        }

        try {
            Optional<Schedule> existingSchedule = scheduleRepository.findByStudentId(String.valueOf(studentId));

            if (existingSchedule.isEmpty()) {
                return false;
            }
            if (newSchedule.getSubjects() == null || newSchedule.getSubjects().isEmpty()) {
                return false;
            }

            return existingSchedule.get().getClassSessions().stream()
                    .anyMatch(class1 -> newSchedule.getClassSessions().stream()
                            .anyMatch(class2 -> hasTimeConflict(class1, class2)));

        } catch (Exception e) {
            throw new RuntimeException("Failed to check schedule conflicts", e);
        }
    }

    /**
     * Validates if a class session has available capacity.
     *
     * @param classSession the unique identifier of the class session
     * @return true if capacity is available, false otherwise
     * @throws IllegalArgumentException if class session ID is invalid
     * @throws RuntimeException if validation fails
     */
    public boolean validateScheduleCapacity(ClassSession classSession) {
        if (classSession == null) {
            throw new IllegalArgumentException("Class session cannot be null");
        }

        try {
            long enrollmentCount = scheduleRepository.countBySubjectId(classSession.getId());
            int maxCapacity = getClassSessionMaxCapacity(classSession);

            return enrollmentCount < maxCapacity;

        } catch (Exception e) {
            throw new RuntimeException("Failed to validate schedule capacity", e);
        }
    }

    /**
     * Retrieves the current active schedule for a student.
     *
     * @param studentId the unique identifier of the student
     * @return the current active schedule
     * @throws IllegalArgumentException if student ID is null or empty
     * @throws RuntimeException if schedule not found or retrieval fails
     */
    public Schedule getCurrentSchedule(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        try {
            return scheduleRepository.findCurrentScheduleByStudentId(studentId.trim())
                    .orElseThrow(() -> new RuntimeException("No current schedule found for student ID: " + studentId));

        } catch (Exception e) {
            throw new RuntimeException("Failed to get current schedule", e);
        }
    }

    /**
     * Retrieves the complete schedule history for a student.
     * Returns all past schedules excluding the current one.
     *
     * @param studentId the unique identifier of the student
     * @return a list of historical schedules
     * @throws IllegalArgumentException if student ID is null or empty
     * @throws RuntimeException if retrieval fails
     */
    public List<Schedule> getScheduleHistory(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        try {
            return scheduleRepository.findScheduleHistoryByStudentId(studentId.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get schedule history", e);
        }
    }

    /**
     * Helper method to check if two subjects have time conflicts.
     *
     * @param subject1 the first subject
     * @param subject2 the second subject
     * @return true if there is a time conflict, false otherwise
     */
    private boolean hasTimeConflict(ClassSession subject1, ClassSession subject2) {
        LocalDateTime start1 = subject1.getStartDate();
        LocalDateTime end1 = subject1.getEndDate();
        LocalDateTime start2 = subject2.getStartDate();
        LocalDateTime end2 = subject2.getEndDate();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Helper method to get the maximum capacity of a class session.
     *
     * @param classSession the class session identifier
     * @return the maximum capacity
     */
    private int getClassSessionMaxCapacity(ClassSession classSession) {
        return classSession.getCapacity();
    }
}