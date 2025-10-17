package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.TrafficLightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Data Transfer Object for manager response operations.
 * Contains information returned for deanery management operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for manager responses in deanery management")
public class ManagerResponseDTO {

    /**
     * List of petitions in the manager's deanery
     */
    @Schema(description = "List of petitions assigned to the deanery")
    private List<PetitionSummary> petitions;

    /**
     * Student's current schedule information
     */
    @Schema(description = "Current schedule of the requesting student")
    private StudentSchedule studentSchedule;

    /**
     * Academic traffic light status of the student
     */
    @Schema(description = "Academic status indicator of the student")
    private AcademicStatus academicStatus;

    /**
     * Alternative groups availability information
     */
    @Schema(description = "Available alternative groups for subject change")
    private List<GroupAvailability> alternativeGroups;

    /**
     * Current change period configuration
     */
    @Schema(description = "Active change periods configuration")
    private List<ChangePeriod> changePeriods;

    /**
     * Group capacity monitoring alerts
     */
    @Schema(description = "Capacity monitoring alerts for groups")
    private List<CapacityAlert> capacityAlerts;

    /**
     * Operation result message
     */
    @Schema(description = "Result message of the operation", example = "Petition processed successfully")
    private String message;

    /**
     * Success status of the operation
     */
    @Schema(description = "Indicates if the operation was successful", example = "true")
    private Boolean success;

    /**
     * Dashboard statistics
     */
    @Schema(description = "Total number of petitions in the deanery", example = "150")
    private Integer totalPetitions;

    /**
     * Number of pending petitions
     */
    @Schema(description = "Number of pending petitions", example = "75")
    private Integer pendingPetitions;

    /**
     * Number of approved petitions
     */
    @Schema(description = "Number of approved petitions", example = "50")
    private Integer approvedPetitions;

    /**
     * Number of rejected petitions
     */
    @Schema(description = "Number of rejected petitions", example = "25")
    private Integer rejectedPetitions;


    /**
     * Nested static classes for detailed components of the response
     */

    /**
     * Summary of a petition
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetitionSummary {
        @Schema(description = "Petition unique identifier", example = "petition-123")
        private String petitionId;

        @Schema(description = "Student Id who made the petition", example = "1000098136")
        private String studentId;

        @Schema(description = "Subject involved in the petition", example = "CALC1 - Cálculo I")
        private String subject;

        @Schema(description = "Type of petition", example = "GROUP_CHANGE")
        private String petitionType;

        @Schema(description = "Current state of the petition", example = "PENDING")
        private String state;

        @Schema(description = "Priority level", example = "MEDIUM")
        private String priority;

        @Schema(description = "Date when petition was created")
        private LocalDateTime creationDate;

        @Schema(description = "Justification provided by student")
        private String justification;
    }

    /**
     * Student's schedule details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSchedule {
        @Schema(description = "Student identifier", example = "student-456")
        private String studentId;

        @Schema(description = "Student full name", example = "Juan Carlos Pérez")
        private String studentName;

        @Schema(description = "Current semester", example = "2024-1")
        private String semester;

        @Schema(description = "List of enrolled sessions")
        private List<ScheduleEntry> sessions;
    }

    /**
     * Details of a schedule entry
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleEntry {
        @Schema(description = "Subject short name", example = "CALC1")
        private String subjectShortName;

        @Schema(description = "Subject full name", example = "Cálculo I")
        private String subjectName;

        @Schema(description = "Professor name", example = "Dr. María González")
        private String professorName;

        @Schema(description = "Day of the week", example = "MONDAY")
        private String dayOfWeek;

        @Schema(description = "Start time of the class")
        private LocalTime startTime;

        @Schema(description = "End time of the class")
        private LocalTime endTime;

        @Schema(description = "Classroom location", example = "A-201")
        private String classroom;
    }

    /**
     * Academic status details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicStatus {
        @Schema(description = "Student identifier", example = "1000098136")
        private String studentId;

        @Schema(description = "Student full name", example = "Juan Carlos Pérez")
        private String studentName;

        @Schema(description = "Traffic light status", allowableValues = {"GREEN", "BLUE", "RED"}, example = "GREEN")
        private TrafficLightStatus trafficLightStatus;

        @Schema(description = "Status description", example = "Normal academic progress")
        private String description;

        @Schema(description = "Current GPA", example = "4.2")
        private Double gpa;

        @Schema(description = "Number of approved subjects", example = "15")
        private Integer approvedSubjects;

        @Schema(description = "Number of failed subjects", example = "0")
        private Integer failedSubjects;

        @Schema(description = "Number of ongoing subjects", example = "5")
        private Integer ongoingSubjects;

        @Schema(description = "Credit percentage completed", example = "75.5")
        private Double creditPercentage;
    }

    /**
     * Details of group availability for subject change
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupAvailability {
        @Schema(description = "Session identifier", example = "session-789")
        private String sessionId;

        @Schema(description = "Subject short name", example = "CALC1")
        private String subjectShortName;

        @Schema(description = "Group identifier", example = "Group 02")
        private String groupId;

        @Schema(description = "Professor name", example = "Dr. Carlos Ruiz")
        private String professorName;

        @Schema(description = "Available capacity", example = "5")
        private Integer availableSpots;

        @Schema(description = "Total capacity", example = "30")
        private Integer totalCapacity;

        @Schema(description = "Current enrollment", example = "25")
        private Integer currentEnrollment;

        @Schema(description = "Waiting list size", example = "3")
        private Integer waitingListSize;

        @Schema(description = "Schedule information")
        private List<ScheduleEntry> schedule;

        @Schema(description = "Indicates if group is available for enrollment", example = "true")
        private Boolean available;
    }

    /**
     * Details of a change period
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePeriod {
        @Schema(description = "Period identifier", example = "period-2024-1")
        private String periodId;

        @Schema(description = "Period name", example = "Change Period January 2024")
        private String periodName;

        @Schema(description = "Start date of the change period")
        private LocalDateTime startDate;

        @Schema(description = "End date of the change period")
        private LocalDateTime endDate;

        @Schema(description = "Indicates if period is currently active", example = "true")
        private Boolean active;

        @Schema(description = "Description of the period", example = "Regular change period for spring semester")
        private String description;
    }

    /**
     * Details of a capacity alert
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityAlert {
        @Schema(description = "Session identifier", example = "session-789")
        private String sessionId;

        @Schema(description = "Subject information", example = "CALC1 - Cálculo I")
        private String subject;

        @Schema(description = "Current enrollment", example = "27")
        private Integer currentEnrollment;

        @Schema(description = "Total capacity", example = "30")
        private Integer totalCapacity;

        @Schema(description = "Occupancy percentage", example = "90.0")
        private Double occupancyPercentage;

        @Schema(description = "Alert level", allowableValues = {"WARNING", "CRITICAL"}, example = "WARNING")
        private String alertLevel;

        @Schema(description = "Alert message", example = "Group is at 90% capacity")
        private String alertMessage;

        @Schema(description = "Date when alert was generated")
        private LocalDateTime alertDate;
    }

    @Data
    @Schema(description = "Respuesta con información de decanatura")
    public static class DeaneryInfo {

        @Schema(description = "ID de la decanatura", example = "sistemas-15")
        private String deaneryId;
        @Schema(description = "Nombre de la decanatura", example = "Decanatura de Ingeniería de Sistemas")
        private String deaneryName;

        @Schema(description = "Nombre del decano", example = "Carlos Andrés Pérez")
        private String deanName;

        @Schema(description = "Lista de nombres de profesores asociados a la decanatura")
        private List<String> professorNames;
        
        @Schema(description = "Lista de nombres de programas académicos asociados a la decanatura")
        private List<String> academicProgramNames;
    }

    @Data
    public static class AcademicProgramInfo {
        @Schema(description = "ID del programa académico", example = "sistemas-15")
        private String id;
        @Schema(description = "Nombre del programa académico", example = "Ingeniería de Sistemas")
        private String name;
        @Schema(description = "Nombre de la decanatura a la que pertenece", example = "Decanatura de Ingeniería de Sistemas")   
        private String deaneryName;
        
    }

    @Data
    public static class DeanResponseDTO {
        @Schema(description = "Unique identifier of the dean", example = "1000098653")
        private String deanCode;

        @Schema(description = "Full name of the dean", example = "Juan Perez")
        private String name;

        @Schema(description = "Email address of the dean", example = "juan.perez@mail.escuelaing.edu.co")
        private String mail;

        @Schema(description = "Document number of the dean", example = "123456789")
        private String document;
    }
    
}
