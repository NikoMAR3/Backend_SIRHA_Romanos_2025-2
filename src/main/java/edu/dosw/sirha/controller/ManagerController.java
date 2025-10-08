package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.ManagerRequestDTO;
import edu.dosw.sirha.controller.dtos.ManagerResponseDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing administrative operations by deans and academic vice presidents.
 * Provides endpoints for petition management, student information access, and system configuration.
 *
 * @author SIRHA Development Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/manager")
@Tag(name = "Managers Management", description = "Endpoints para gestión de decanatura")
public class ManagerController {

    private final DeaneryService deaneryService;
    private final DeanService deanService;
    private final AcademicVicePresidentService academicVicePresidentService;
    private final ClassSessionService classSessionService;
    private final PeriodService periodService;
    private final ObserverService observerService;
    private final PetitionService petitionService;
    private final StudentService studentService;
    private final TrafficLightService trafficLightService;

    /**
     * Constructor for ManagerController.
     *
     * @param deaneryService service for deanery operations
     * @param deanService service for dean operations
     * @param academicVicePresidentService service for academic vice president operations
     * @param classSessionService service for class session operations
     * @param periodService service for period operations
     * @param observerService service for monitoring operations
     * @param petitionService service for petition operations
     * @param studentService service for student operations
     * @param trafficLightService service for traffic light operations
     */
    public ManagerController(DeaneryService deaneryService,
                             DeanService deanService,
                             AcademicVicePresidentService academicVicePresidentService,
                             ClassSessionService classSessionService,
                             PeriodService periodService,
                             ObserverService observerService,
                             PetitionService petitionService,
                             StudentService studentService,
                             TrafficLightService trafficLightService) {
        this.deaneryService = deaneryService;
        this.deanService = deanService;
        this.academicVicePresidentService = academicVicePresidentService;
        this.classSessionService = classSessionService;
        this.periodService = periodService;
        this.observerService = observerService;
        this.petitionService = petitionService;
        this.studentService = studentService;
        this.trafficLightService = trafficLightService;
    }

    /**
     * Retrieves all petitions for a specific deanery based on manager credentials.
     * Access is restricted by faculty jurisdiction.
     *
     * @param managerId unique identifier of the manager
     * @param managerType type of manager (DEAN or ACADEMIC_VICEPRESIDENT)
     * @return ResponseEntity containing list of petitions for the deanery
     * @throws IllegalArgumentException if manager credentials are invalid
     * @throws RuntimeException if manager type is not supported
     */
    @GetMapping("/petitions")
    @Operation(summary = "Obtener solicitudes de la decanatura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitudes obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Credenciales de manager inválidas"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ManagerResponseDTO> getDeaneryPetitions(
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType) {

        validateManagerAccess(managerId, managerType);
        String deaneryName = getDeaneryForManager(managerId, managerType);

        List<Petition> petitions = petitionService.searchPetitionsByDeanery(deaneryName);

        List<ManagerResponseDTO.PetitionSummary> petitionSummaryList = petitions.stream()
                .map(this::mapToPetitionSummary)
                .collect(Collectors.toList());

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setPetitions(petitionSummaryList);
        response.setSuccess(true);
        response.setMessage("Solicitudes obtenidas exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the complete schedule for a specific student.
     * Requires manager authentication and returns detailed schedule information.
     *
     * @param studentId unique identifier of the student
     * @param managerId unique identifier of the manager
     * @param managerType type of manager requesting the information
     * @return ResponseEntity containing student schedule details
     * @throws IllegalArgumentException if student or manager not found
     * @throws RuntimeException if schedule cannot be retrieved
     */
    @GetMapping("/student-schedule/{studentId}")
    @Operation(summary = "Obtener horario del estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horario obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getStudentSchedule(
            @Parameter(description = "ID del estudiante") @PathVariable String studentId,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType) {

        validateManagerAccess(managerId, managerType);

        Student student = studentService.searchStudentById(studentId);
        Schedule schedule = studentService.getStudentSchedule(studentId);

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setStudentSchedule(mapToStudentSchedule(student, schedule));
        response.setSuccess(true);
        response.setMessage("Horario obtenido exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the academic traffic light status for a specific student.
     * Includes GPA calculation and detailed academic progress information.
     *
     * @param studentId unique identifier of the student
     * @param managerId unique identifier of the manager
     * @param managerType type of manager requesting the information
     * @return ResponseEntity containing academic status details
     * @throws IllegalArgumentException if student not found
     * @throws RuntimeException if traffic light information is unavailable
     */
    @GetMapping("/academic-status/{studentId}")
    @Operation(summary = "Consultar semáforo académico del estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Semáforo académico obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Estudiante o semáforo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getStudentAcademicStatus(
            @Parameter(description = "ID del estudiante") @PathVariable String studentId,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType) {

        validateManagerAccess(managerId, managerType);

        Student student = studentService.searchStudentById(studentId);
        TrafficLight trafficLight = trafficLightService.searchTrafficLightByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Semáforo académico no encontrado"));

        double gpa = trafficLightService.calculateGPA(studentId);

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setAcademicStatus(mapToAcademicStatus(student, trafficLight, gpa));
        response.setSuccess(true);
        response.setMessage("Semáforo académico obtenido exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves alternative groups available for a specific subject.
     * Shows capacity, enrollment, and availability information for decision making.
     *
     * @param subjectShortName short code of the subject
     * @param managerId unique identifier of the manager
     * @param managerType type of manager requesting the information
     * @return ResponseEntity containing alternative group availability
     * @throws IllegalArgumentException if subject not found
     * @throws RuntimeException if group information cannot be retrieved
     */
    @GetMapping("/alternative-groups")
    @Operation(summary = "Consultar grupos alternativos disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupos alternativos obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getAlternativeGroups(
            @Parameter(description = "Código corto de la materia") @RequestParam String subjectShortName,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType) {

        validateManagerAccess(managerId, managerType);

        List<ClassSession> sessions = classSessionService.searchSessionsBySubjectShortName(subjectShortName);

        List<ManagerResponseDTO.GroupAvailability> alternatives = sessions.stream()
                .map(this::mapToGroupAvailability)
                .collect(Collectors.toList());

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setAlternativeGroups(alternatives);
        response.setSuccess(true);
        response.setMessage("Grupos alternativos obtenidos exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Processes a response to a petition (approve, reject, or request additional information).
     * Updates petition status and maintains decision history.
     *
     * @param petitionId unique identifier of the petition
     * @param request contains the decision and justification
     * @param managerId unique identifier of the manager
     * @param managerType type of manager responding to the petition
     * @return ResponseEntity containing updated petition information
     * @throws IllegalArgumentException if petition not found or invalid decision
     * @throws RuntimeException if manager lacks permission or update fails
     */
    @PutMapping("/petition/{petitionId}/respond")
    @Operation(summary = "Responder a una solicitud")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta registrada exitosamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para responder esta solicitud"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "400", description = "Decisión inválida")
    })
    public ResponseEntity<ManagerResponseDTO> respondToPetition(
            @Parameter(description = "ID de la solicitud") @PathVariable String petitionId,
            @RequestBody ManagerRequestDTO request,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType) {

        validateManagerAccess(managerId, managerType);

        Petition petition = petitionService.searchPetitionsById(petitionId);

        String managerDeanery = getDeaneryForManager(managerId, managerType);
        if (managerDeanery != null && !petition.getAssociateDeanery().equals(managerDeanery)) {
            throw new IllegalArgumentException("No tiene permisos para responder esta solicitud");
        }

        switch (request.getDecision().toLowerCase()) {
            case "approve":
                petition = petitionService.changePetitionState(petitionId, PetitionState.APPROVED);
                break;
            case "reject":
                petition = petitionService.changePetitionState(petitionId, PetitionState.REPROVED);
                petition.setRejectionReason(request.getJustification());
                petition = petitionService.modifyPetition(petition);
                break;
            case "request_info":
                petition = petitionService.changePetitionState(petitionId, PetitionState.PENDING);
                break;
            default:
                throw new IllegalArgumentException("Acción no válida: " + request.getDecision());
        }

        List<String> history = petition.getDecisionHistory();
        if (history == null) history = new ArrayList<>();
        history.add(LocalDateTime.now() + ": " + request.getDecision() + " por " + managerId);
        petition.setDecisionHistory(history);
        petition.setAssignedReviewer(managerId);

        petition = petitionService.modifyPetition(petition);

        ManagerResponseDTO response = new ManagerResponseDTO();
        List<ManagerResponseDTO.PetitionSummary> petitionList = new ArrayList<>();
        petitionList.add(mapToPetitionSummary(petition));
        response.setPetitions(petitionList);
        response.setSuccess(true);
        response.setMessage("Respuesta registrada exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Configures enabled periods for student changes and modifications.
     * Sets up timeframes when students can submit change requests.
     *
     * @param request contains period start and end dates
     * @param managerId unique identifier of the manager
     * @param managerType type of manager configuring the period
     * @return ResponseEntity confirming period configuration
     * @throws IllegalArgumentException if dates are invalid
     * @throws RuntimeException if period creation fails
     */
    @PostMapping("/periods")
    @Operation(summary = "Configurar periodo de cambios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Periodo configurado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Fechas inválidas"),
            @ApiResponse(responseCode = "500", description = "Error al crear periodo")
    })
    public ResponseEntity<ManagerResponseDTO> configurePeriod(
            @RequestBody ManagerRequestDTO request,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType) {

        validateManagerAccess(managerId, managerType);

        if (request.getPeriodStartDate() == null || request.getPeriodEndDate() == null) {
            throw new IllegalArgumentException("Fechas de inicio y fin son obligatorias");
        }

        if (request.getPeriodStartDate().isAfter(request.getPeriodEndDate())) {
            throw new IllegalArgumentException("Fecha de inicio debe ser anterior a la fecha de fin");
        }

        Period period = new Period();
        period.setStartDate(request.getPeriodStartDate().toString());
        period.setEndDate(request.getPeriodEndDate().toString());
        period.setEnabled(true);

        Period savedPeriod = periodService.createPeriod(period);

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setSuccess(true);
        response.setMessage("Periodo configurado exitosamente para el rango: " +
                savedPeriod.getStartDate() + " - " + savedPeriod.getEndDate());

        return ResponseEntity.ok(response);
    }

    /**
     * Monitors class session capacity and generates alerts when threshold is reached.
     * Helps administrators manage group enrollment and prevent overcrowding.
     *
     * @param managerId unique identifier of the manager
     * @param managerType type of manager requesting the alerts
     * @param threshold capacity percentage threshold for alerts (default: 90)
     * @return ResponseEntity containing capacity alert information
     * @throws IllegalArgumentException if threshold is invalid
     * @throws RuntimeException if monitoring fails
     */
    @GetMapping("/capacity-alerts")
    @Operation(summary = "Obtener alertas de capacidad de grupos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alertas de capacidad obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Umbral inválido"),
            @ApiResponse(responseCode = "500", description = "Error en monitoreo")
    })
    public ResponseEntity<ManagerResponseDTO> getCapacityAlerts(
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            @Parameter(description = "Umbral de capacidad") @RequestParam(required = false, defaultValue = "90") Integer threshold) {

        validateManagerAccess(managerId, managerType);

        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Umbral debe estar entre 0 y 100");
        }

        observerService.monitorAllClassSessions();

        String deaneryName = getDeaneryForManager(managerId, managerType);
        List<ClassSession> allSessions = classSessionService.searchAllSessions();

        List<ManagerResponseDTO.CapacityAlert> alerts = allSessions.stream()
                .filter(session -> {
                    double occupancyPercentage = (double) session.getEnrolledStudents() / session.getCapacity() * 100;
                    return occupancyPercentage >= threshold;
                })
                .map(this::mapToCapacityAlert)
                .collect(Collectors.toList());

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setCapacityAlerts(alerts);
        response.setSuccess(true);
        response.setMessage("Alertas de capacidad obtenidas exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Validates manager access credentials and permissions.
     *
     * @param managerId unique identifier of the manager
     * @param managerType type of manager (DEAN or ACADEMIC_VICEPRESIDENT)
     * @throws IllegalArgumentException if manager not found
     * @throws RuntimeException if manager type is invalid
     */
    private void validateManagerAccess(String managerId, String managerType) {
        if (managerId == null || managerId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID de manager es obligatorio");
        }

        if (managerType == null || managerType.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de manager es obligatorio");
        }

        switch (managerType.toUpperCase()) {
            case "DEAN":
                deanService.searchDeanById(managerId);
                break;
            case "ACADEMIC_VICEPRESIDENT":
                academicVicePresidentService.searchAcademicVicePresidentById(managerId);
                break;
            default:
                throw new RuntimeException("Tipo de manager no válido: " + managerType);
        }
    }

    /**
     * Retrieves the deanery name associated with a manager.
     *
     * @param managerId unique identifier of the manager
     * @param managerType type of manager
     * @return deanery name for deans, null for academic vice presidents (access to all deaneries)
     */
    private String getDeaneryForManager(String managerId, String managerType) {
        if ("DEAN".equalsIgnoreCase(managerType)) {
            Dean dean = deanService.searchDeanById(managerId);
            return dean.getDeanery() != null ? dean.getDeanery().getDeaneryName() : null;
        }
        return null;
    }

    /**
     * Calculates the credit percentage completion for a student based on their traffic light status.
     *
     * @param trafficLight traffic light object containing academic progress information
     * @return percentage of credits completed
     */
    private Double calculateCreditPercentage(TrafficLight trafficLight) {
        int approvedCredits = 0;
        int totalCredits = 0;

        for (Subject subject : trafficLight.getApprovedSubjects().keySet()) {
            approvedCredits += subject.getCredits();
            totalCredits += subject.getCredits();
        }

        for (Subject subject : trafficLight.getFailedSubjects().keySet()) {
            totalCredits += subject.getCredits();
        }

        for (Subject subject : trafficLight.getOnGoingSubjects()) {
            totalCredits += subject.getCredits();
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        return ((double) approvedCredits / totalCredits) * 100;
    }

    /**
     * Maps a Petition entity to a PetitionSummary DTO.
     *
     * @param petition petition entity to map
     * @return mapped petition summary DTO
     */
    private ManagerResponseDTO.PetitionSummary mapToPetitionSummary(Petition petition) {
        ManagerResponseDTO.PetitionSummary summary = new ManagerResponseDTO.PetitionSummary();
        summary.setPetitionId(petition.getPetitionId());
        summary.setStudentId(petition.getStudentId());
        summary.setSubject(petition.getSubjectShortName() + " - " + petition.getSubjectName());
        summary.setPetitionType(petition.getType().toString());
        summary.setState(petition.getState().toString());
        summary.setPriority(petition.getPriority().toString());
        summary.setCreationDate(petition.getCreationDate());
        summary.setJustification(petition.getJustification());
        return summary;
    }

    /**
     * Maps Student and Schedule entities to a StudentSchedule DTO.
     *
     * @param student student entity
     * @param schedule schedule entity
     * @return mapped student schedule DTO
     */
    private ManagerResponseDTO.StudentSchedule mapToStudentSchedule(Student student, Schedule schedule) {
        ManagerResponseDTO.StudentSchedule studentSchedule = new ManagerResponseDTO.StudentSchedule();
        studentSchedule.setStudentId(student.getId());
        studentSchedule.setStudentName(student.getName());
        studentSchedule.setSemester(String.valueOf(schedule.getSemester()));

        List<ManagerResponseDTO.ScheduleEntry> scheduleEntries = new ArrayList<>();
        ManagerResponseDTO.ScheduleEntry entry = new ManagerResponseDTO.ScheduleEntry();
        entry.setSubjectShortName(schedule.getSubjectShortName());
        entry.setSubjectName(schedule.getName());
        entry.setProfessorName(schedule.getProfessor() != null ? schedule.getProfessor().getName() : "N/A");
        entry.setDayOfWeek(schedule.getDayOfWeek());
        entry.setStartTime(schedule.getStartTime().toLocalTime());
        entry.setEndTime(schedule.getEndTime().toLocalTime());
        entry.setClassroom(schedule.getClassroom());
        scheduleEntries.add(entry);

        studentSchedule.setSessions(scheduleEntries);
        return studentSchedule;
    }

    /**
     * Maps Student, TrafficLight, and GPA information to an AcademicStatus DTO.
     *
     * @param student student entity
     * @param trafficLight traffic light entity
     * @param gpa calculated GPA
     * @return mapped academic status DTO
     */
    private ManagerResponseDTO.AcademicStatus mapToAcademicStatus(Student student, TrafficLight trafficLight, double gpa) {
        ManagerResponseDTO.AcademicStatus academicStatus = new ManagerResponseDTO.AcademicStatus();
        academicStatus.setStudentId(student.getId());
        academicStatus.setStudentName(student.getName());
        academicStatus.setTrafficLightStatus(trafficLight.getStatus());
        academicStatus.setDescription("Estado académico del estudiante " + student.getName() + ": " + trafficLight.getStatus().getDescription());
        academicStatus.setGpa(gpa);
        academicStatus.setApprovedSubjects(trafficLight.getApprovedSubjects().size());
        academicStatus.setFailedSubjects(trafficLight.getFailedSubjects().size());
        academicStatus.setOngoingSubjects(trafficLight.getOnGoingSubjects().size());

        Double creditPercentage = calculateCreditPercentage(trafficLight);
        academicStatus.setCreditPercentage(creditPercentage);

        return academicStatus;
    }

    /**
     * Maps a ClassSession entity to a GroupAvailability DTO.
     *
     * @param session class session entity
     * @return mapped group availability DTO
     */
    private ManagerResponseDTO.GroupAvailability mapToGroupAvailability(ClassSession session) {
        ManagerResponseDTO.GroupAvailability availability = new ManagerResponseDTO.GroupAvailability();
        availability.setSessionId(session.getId());
        availability.setSubjectShortName(session.getSubjectShortName());
        availability.setGroupId("Grupo " + session.getId().substring(0, 2));
        availability.setProfessorName(session.getProfessor() != null ? session.getProfessor().getName() : "N/A");
        availability.setCurrentEnrollment(session.getEnrolledStudents());
        availability.setTotalCapacity(session.getCapacity());
        availability.setAvailableSpots(session.getCapacity() - session.getEnrolledStudents());
        availability.setWaitingListSize(session.getWaitingListStudentIds() != null ?
                session.getWaitingListStudentIds().size() : 0);
        availability.setAvailable(session.getEnrolledStudents() < session.getCapacity());
        return availability;
    }

    /**
     * Maps a ClassSession entity to a CapacityAlert DTO.
     *
     * @param session class session entity
     * @return mapped capacity alert DTO
     */
    private ManagerResponseDTO.CapacityAlert mapToCapacityAlert(ClassSession session) {
        ManagerResponseDTO.CapacityAlert alert = new ManagerResponseDTO.CapacityAlert();
        alert.setSessionId(session.getId());
        alert.setSubject(session.getSubjectShortName());
        alert.setCurrentEnrollment(session.getEnrolledStudents());
        alert.setTotalCapacity(session.getCapacity());
        alert.setOccupancyPercentage((double) session.getEnrolledStudents() / session.getCapacity() * 100);
        return alert;
    }
}
