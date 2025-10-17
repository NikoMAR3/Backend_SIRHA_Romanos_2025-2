package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.controller.dtos.GroupsResponseDTO;
import edu.dosw.sirha.controller.dtos.ManagerRequestDTO;
import edu.dosw.sirha.controller.dtos.ManagerResponseDTO;
import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.*;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

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

    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    private final DeaneryService deaneryService;
    private final DeanService deanService;
    private final AcademicVicePresidentService academicVicePresidentService;
    private final ClassSessionService classSessionService;
    private final PeriodService periodService;
    private final ObserverService observerService;
    private final PetitionService petitionService;
    private final StudentService studentService;
    private final TrafficLightService trafficLightService;
    private final SubjectService subjectService;
    private final AcademicProgramService academicProgramService;
    private final ProfessorService professorService;


    /**
     * Constructor for ManagerController.
     */
    public ManagerController(DeaneryService deaneryService,
                             DeanService deanService,
                             AcademicVicePresidentService academicVicePresidentService,
                             ClassSessionService classSessionService,
                             PeriodService periodService,
                             ObserverService observerService,
                             PetitionService petitionService,
                             StudentService studentService,
                             TrafficLightService trafficLightService, SubjectService subjectService, AcademicProgramService academicProgramService, ProfessorService professorService) {
                                
        this.professorService = professorService;
        this.deaneryService = deaneryService;
        this.deanService = deanService;
        this.academicVicePresidentService = academicVicePresidentService;
        this.classSessionService = classSessionService;
        this.periodService = periodService;
        this.observerService = observerService;
        this.petitionService = petitionService;
        this.studentService = studentService;
        this.trafficLightService = trafficLightService;
        this.subjectService = subjectService;
        this.academicProgramService = academicProgramService;
    }

    /**
     * Retrieves all petitions for a specific deanery based on manager credentials - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Access is restricted by faculty jurisdiction.
     */
    @GetMapping("/petitions")
    @Operation(summary = "Obtener solicitudes de la decanatura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitudes obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Credenciales de manager inválidas"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ManagerResponseDTO> getDeaneryPetitions(
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Getting petitions for manager {} of type {}", managerId, managerType);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (!currentUser.getId().equals(managerId)) {
            logger.warn("User {} attempted to access petitions for manager {}", currentUser.getId(), managerId);
            throw new IllegalArgumentException("No puedes acceder a solicitudes de otro manager");
        }

        
        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            logger.warn("User {} with type {} attempted to access as manager type {}", 
                       currentUser.getId(), currentUser.getType(), managerType);
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);
        String deaneryName = getDeaneryForManager(managerId, managerType);

        List<Petition> petitions;
        if (deaneryName != null) {
            
            petitions = petitionService.searchPetitionsByDeanery(deaneryName);
            logger.info("Dean {} retrieved {} petitions for deanery {}", managerId, petitions.size(), deaneryName);
        } else {
        
            petitions = petitionService.searchAllPetitions();
            logger.info("Academic VP {} retrieved {} total petitions", managerId, petitions.size());
        }

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
     * Retrieves the complete schedule for a specific student - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Requires manager authentication and returns detailed schedule information.
     */
    @GetMapping("/student-schedule/student/{studentId}")
    @Operation(summary = "Obtener horario del estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horario obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getStudentSchedule(
            @Parameter(description = "ID del estudiante") @PathVariable String studentId,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Getting schedule for student {} requested by manager {}", studentId, managerId);

    
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

  
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder a información como otro manager");
        }


        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        Student student = studentService.searchStudentById(studentId);
        Schedule schedule = studentService.getStudentSchedule(studentId);

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setStudentSchedule(mapToStudentSchedule(student, schedule));
        response.setSuccess(true);
        response.setMessage("Horario obtenido exitosamente");

        logger.info("Schedule retrieved successfully for student {} by manager {}", studentId, managerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the complete schedule for the student of an specific petition - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Requires manager authentication and returns detailed schedule information.
     */
    @GetMapping("/student-schedule/petition/{petitionId}")
    @Operation(summary = "Obtener horario del estudiante de la petición ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horario obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getStudentScheduleWithPetition(
            @Parameter(description = "ID de la solicitud") @PathVariable String petitionId,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Getting schedule for student from petition {} requested by manager {}", petitionId, managerId);


        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session,
                UserType.DEAN,
                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);


        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder a información como otro manager");
        }


        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        Petition petition = petitionService.searchPetitionsById(petitionId);
        Student student = studentService.searchStudentById(petition.getStudentId());
        Schedule schedule = studentService.getStudentSchedule(petition.getStudentId());

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setStudentSchedule(mapToStudentSchedule(student, schedule));
        response.setSuccess(true);
        response.setMessage("Horario obtenido exitosamente");

        logger.info("Schedule retrieved successfully for student with petition {} by manager {}", petitionId, managerId);
        return ResponseEntity.ok(response);
    }



    /**
     * Retrieves the academic traffic light status for a specific student - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Includes GPA calculation and detailed academic progress information.
     */
    @GetMapping("/academic-status/{studentId}")
    @Operation(summary = "Consultar semáforo académico del estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Semáforo académico obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Estudiante o semáforo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getStudentAcademicStatus(
            @Parameter(description = "ID del estudiante") @PathVariable String studentId,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Getting academic status for student {} requested by manager {}", studentId, managerId);


        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

       
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder a información como otro manager");
        }

   
        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        Student student = studentService.searchStudentById(studentId);
        TrafficLight trafficLight = trafficLightService.searchTrafficLightByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Semáforo académico no encontrado"));

        double gpa = trafficLightService.calculateGPA(studentId);

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setAcademicStatus(mapToAcademicStatus(student, trafficLight, gpa));
        response.setSuccess(true);
        response.setMessage("Semáforo académico obtenido exitosamente");

        logger.info("Academic status retrieved successfully for student {} by manager {}", studentId, managerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves alternative groups available for a specific subject - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Shows capacity, enrollment, and availability information for decision making.
     */
    @GetMapping("/alternative-groups")
    @Operation(summary = "Consultar grupos alternativos disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupos alternativos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<ManagerResponseDTO> getAlternativeGroups(
            @Parameter(description = "Código corto de la materia") @RequestParam String subjectShortName,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Getting alternative groups for subject {} requested by manager {}", subjectShortName, managerId);


        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

    
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder a información como otro manager");
        }

        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        List<ClassSession> sessions = classSessionService.searchSessionsBySubjectShortName(subjectShortName);

        List<ManagerResponseDTO.GroupAvailability> alternatives = sessions.stream()
                .map(this::mapToGroupAvailability)
                .collect(Collectors.toList());

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setAlternativeGroups(alternatives);
        response.setSuccess(true);
        response.setMessage("Grupos alternativos obtenidos exitosamente");

        logger.info("Alternative groups retrieved successfully for subject {} by manager {}", subjectShortName, managerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Processes a response to a petition - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Updates petition status and maintains decision history.
     */
    @PutMapping("/petition/{petitionId}/respond")
    @Operation(summary = "Responder a una solicitud")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta registrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para responder esta solicitud"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "400", description = "Decisión inválida")
    })
    public ResponseEntity<ManagerResponseDTO> respondToPetition(
            @Parameter(description = "ID de la solicitud") @PathVariable String petitionId,
            @RequestBody ManagerRequestDTO request,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Manager {} responding to petition {} with decision: {}", managerId, petitionId, request.getDecision());

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para responder solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

       
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes responder solicitudes como otro manager");
        }

      
        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        Petition petition = petitionService.searchPetitionsById(petitionId);


        String managerDeanery = getDeaneryForManager(managerId, managerType);
        if (managerDeanery != null && !petition.getAssociateDeanery().equals(managerDeanery)) {
            logger.warn("Dean {} attempted to respond to petition {} outside their deanery", managerId, petitionId);
            throw new IllegalArgumentException("No tiene permisos para responder esta solicitud - fuera de su decanatura");
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
        history.add(LocalDateTime.now() + ": " + request.getDecision() + " por " + managerId + " (" + currentUser.getType() + ")");
        petition.setDecisionHistory(history);
        petition.setAssignedReviewer(managerId);

        petition = petitionService.modifyPetition(petition);

        ManagerResponseDTO response = new ManagerResponseDTO();
        List<ManagerResponseDTO.PetitionSummary> petitionList = new ArrayList<>();
        petitionList.add(mapToPetitionSummary(petition));
        response.setPetitions(petitionList);
        response.setSuccess(true);
        response.setMessage("Respuesta registrada exitosamente");

        logger.info("Petition {} responded successfully by manager {}", petitionId, managerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Configures enabled periods for student changes and modifications - ACADEMIC_VICEPRESIDENT only.
     * Sets up timeframes when students can submit change requests.
     */
    @PostMapping("/periods")
    @Operation(summary = "Configurar periodo de cambios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Periodo configurado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo vicepresidente académico"),
            @ApiResponse(responseCode = "400", description = "Fechas inválidas"),
            @ApiResponse(responseCode = "500", description = "Error al crear periodo")
    })
    public ResponseEntity<ManagerResponseDTO> configurePeriod(
            @RequestBody ManagerRequestDTO request,
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Configuring period by manager {} of type {}", managerId, managerType);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo el vicepresidente académico puede configurar períodos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes configurar períodos como otro manager");
        }

        
        if (!"ACADEMIC_VICEPRESIDENT".equalsIgnoreCase(managerType) || 
            currentUser.getType() != UserType.ACADEMIC_VICEPRESIDENT) {
            throw new IllegalArgumentException("Solo el vicepresidente académico puede configurar períodos");
        }

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

        logger.info("Period configured successfully by academic VP {}", managerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Monitors class session capacity and generates alerts when threshold is reached - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Helps administrators manage group enrollment and prevent overcrowding.
     */
    @GetMapping("/capacity-alerts")
    @Operation(summary = "Obtener alertas de capacidad de grupos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alertas de capacidad obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "400", description = "Umbral inválido"),
            @ApiResponse(responseCode = "500", description = "Error en monitoreo")
    })
    public ResponseEntity<ManagerResponseDTO> getCapacityAlerts(
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            @Parameter(description = "Umbral de capacidad") @RequestParam(required = false, defaultValue = "90") Integer threshold,
            HttpSession session) {

        logger.info("Getting capacity alerts for manager {} with threshold {}%", managerId, threshold);

    
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

       
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder a alertas como otro manager");
        }

        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Umbral debe estar entre 0 y 100");
        }

        observerService.monitorAllClassSessions();

        String deaneryName = getDeaneryForManager(managerId, managerType);
        List<ClassSession> allSessions = classSessionService.searchAllSessions();

        List<ManagerResponseDTO.CapacityAlert> alerts = allSessions.stream()
            .filter(classSession -> {
    
                if (classSession.getCapacity() == 0) {
                    return false;
                }
                double occupancyPercentage = (double) classSession.getEnrolledStudents() / classSession.getCapacity() * 100;
                return occupancyPercentage >= threshold;
            })
            .map(this::mapToCapacityAlert)
            .collect(Collectors.toList());

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setCapacityAlerts(alerts);
        response.setSuccess(true);
        response.setMessage("Alertas de capacidad obtenidas exitosamente");

        logger.info("Capacity alerts retrieved successfully for manager {}: {} alerts found", managerId, alerts.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets manager dashboard summary - DEAN and ACADEMIC_VICEPRESIDENT only.
     * Provides overview of pending petitions, capacity alerts, and key metrics.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Obtener resumen del dashboard de gestión")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico")
    })
    public ResponseEntity<ManagerResponseDTO> getDashboard(
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.info("Getting dashboard for manager {} of type {}", managerId, managerType);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para acceder a esta funcionalidad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder al dashboard como otro manager");
        }

        validateManagerAccess(managerId, managerType);

        String deaneryName = getDeaneryForManager(managerId, managerType);

       
        List<Petition> petitions;
        if (deaneryName != null) {
            
            petitions = petitionService.searchPetitionsByDeanery(deaneryName);
        } else {
        
            petitions = petitionService.searchAllPetitions();
        }

       
        long pendingPetitions = petitions.stream().filter(p -> p.getState() == PetitionState.PENDING).count();
        long approvedPetitions = petitions.stream().filter(p -> p.getState() == PetitionState.APPROVED).count();
        long rejectedPetitions = petitions.stream().filter(p -> p.getState() == PetitionState.REPROVED).count();

        ManagerResponseDTO response = new ManagerResponseDTO();
        response.setSuccess(true);
        response.setMessage("Dashboard obtenido exitosamente");
        
        
        response.setTotalPetitions((int) petitions.size());
        response.setPendingPetitions((int) pendingPetitions);
        response.setApprovedPetitions((int) approvedPetitions);
        response.setRejectedPetitions((int) rejectedPetitions);

        logger.info("Dashboard retrieved successfully for manager {}: {} total petitions", managerId, petitions.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Validates that the manager type matches the user type.
     */
    private boolean isManagerTypeValid(UserType userType, String managerType) {
        switch (userType) {
            case DEAN:
                return "DEAN".equalsIgnoreCase(managerType);
            case ACADEMIC_VICEPRESIDENT:
                return "ACADEMIC_VICEPRESIDENT".equalsIgnoreCase(managerType);
            default:
                return false;
        }
    }

    /**
     * Validates manager access credentials and permissions.
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
                deanService.searchDeanByCode(managerId);
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
     */
    private String getDeaneryForManager(String managerId, String managerType) {
        if ("DEAN".equalsIgnoreCase(managerType)) {
            Dean dean = deanService.searchDeanByCode(managerId);
            return dean.getDeanery() != null ? dean.getDeanery().getDeaneryName() : null;
        }
        return null; 
    }

    /**
     * Calculates the credit percentage completion for a student based on their traffic light status.
     */
    private Double calculateCreditPercentage(TrafficLight trafficLight) {
        int approvedCredits = 0;
        int totalCredits = 0;

        for (String subjectname : trafficLight.getApprovedSubjects().keySet()) {
            Subject subject = subjectService.searchSubjectByFullName(subjectname);
            approvedCredits += subject.getCredits();
            totalCredits += subject.getCredits();
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        return ((double) approvedCredits / totalCredits) * 100;
    }

 

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

    private ManagerResponseDTO.GroupAvailability mapToGroupAvailability(ClassSession session) {
        ManagerResponseDTO.GroupAvailability availability = new ManagerResponseDTO.GroupAvailability();
        availability.setSessionId(session.getId());
        availability.setSubjectShortName(session.getSubjectShortName());
        availability.setGroupId("Grupo " + session.getId().substring(0, Math.min(2, session.getId().length())));
        availability.setProfessorName(session.getProfessor() != null ? session.getProfessor().getName() : "N/A");
        availability.setCurrentEnrollment(session.getEnrolledStudents());
        availability.setTotalCapacity(session.getCapacity());
        availability.setAvailableSpots(session.getCapacity() - session.getEnrolledStudents());
        availability.setWaitingListSize(session.getWaitingListStudentIds() != null ?
                session.getWaitingListStudentIds().size() : 0);
        availability.setAvailable(session.getEnrolledStudents() < session.getCapacity());
        return availability;
    }

    private ManagerResponseDTO.CapacityAlert mapToCapacityAlert(ClassSession session) {
        ManagerResponseDTO.CapacityAlert alert = new ManagerResponseDTO.CapacityAlert();
        alert.setSessionId(session.getId());
        alert.setSubject(session.getSubjectShortName());
        alert.setCurrentEnrollment(session.getEnrolledStudents());
        alert.setTotalCapacity(session.getCapacity());
        alert.setOccupancyPercentage((double) session.getEnrolledStudents() / session.getCapacity() * 100);
        return alert;
    }

    /* */

    /**
     * Generates global traffic light indicators and academic progress summary - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/global-traffic-light-summary")
    @Operation(
            summary = "Indicadores globales de semaforización",
            description = "Genera resumen global de indicadores de avance en planes de estudio"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Indicadores globales generados exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<Map<String, Object>> getGlobalTrafficLightSummary(
            @Parameter(description = "ID del manager") @RequestParam String managerId,
            @Parameter(description = "Tipo de manager") @RequestParam String managerType,
            HttpSession session) {

        logger.debug("Generating global traffic light summary for manager: {}", managerId);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                UserType.DEAN, 
                                                                                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver indicadores globales");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        if (!currentUser.getId().equals(managerId)) {
            throw new IllegalArgumentException("No puedes acceder a indicadores como otro manager");
        }
        if (!isManagerTypeValid(currentUser.getType(), managerType)) {
            throw new IllegalArgumentException("Tipo de manager no coincide con tu rol de usuario");
        }

        validateManagerAccess(managerId, managerType);

        
        String deaneryName = getDeaneryForManager(managerId, managerType);
        Map<String, Object> indicators = generateTrafficLightSummary(deaneryName);

        logger.info("Generated global traffic light summary for manager: {}", managerId);
        return ResponseEntity.ok(indicators);
    }

   

    /**
     * Generates basic traffic light summary.
     */
    private Map<String, Object> generateTrafficLightSummary(String deaneryName) {
        Map<String, Object> summary = new LinkedHashMap<>();
        
    
        List<Student> allStudents = studentService.searchAllStudents();
        
   
        List<Student> students; 
        if (deaneryName != null) {
            students = allStudents.stream()
                    .filter(s -> s.getDeanery() != null && 
                            deaneryName.equals(s.getDeanery().getDeaneryName()))
                    .collect(Collectors.toList());
        } else {
            students = allStudents; 
        }
        
        summary.put("totalStudents", students.size());
        summary.put("scope", deaneryName != null ? deaneryName : "INSTITUTIONAL");
        
        Map<String, Integer> trafficLightCount = new LinkedHashMap<>();
        trafficLightCount.put("GREEN", 0);
        trafficLightCount.put("BLUE", 0);
        trafficLightCount.put("RED", 0);
        trafficLightCount.put("NO_DATA", 0);
        
        for (Student student : students) {
            try {
                Optional<TrafficLight> trafficLight = trafficLightService.searchTrafficLightByStudentId(student.getId());
                if (trafficLight.isPresent()) {
                    String status = trafficLight.get().getStatus().name();
                    trafficLightCount.merge(status, 1, Integer::sum);
                } else {
                    trafficLightCount.merge("NO_DATA", 1, Integer::sum);
                }
            } catch (Exception e) {
                trafficLightCount.merge("NO_DATA", 1, Integer::sum);
            }
        }
        
        summary.put("trafficLightDistribution", trafficLightCount);
        
       
        Map<String, Double> percentages = new LinkedHashMap<>();
        if (students.size() > 0) {
            trafficLightCount.forEach((status, count) -> {
                double percentage = Math.round((double) count / students.size() * 100 * 100.0) / 100.0;
                percentages.put(status, percentage);
            });
        }
        summary.put("percentages", percentages);
        
      
        int redCount = trafficLightCount.get("RED");
        double criticalPercentage = students.size() > 0 ? (double) redCount / students.size() * 100 : 0.0;
        summary.put("studentsInCriticalStatus", redCount);
        summary.put("criticalPercentage", Math.round(criticalPercentage * 100.0) / 100.0);
        
        summary.put("generatedAt", LocalDateTime.now());
        
        return summary;
    }


    //------------------------------------------Decanaturas--------------------------------------------
    private ManagerResponseDTO.DeaneryInfo buildDeaneryResponse(Deanery deanery) {
        ManagerResponseDTO.DeaneryInfo response = new ManagerResponseDTO.DeaneryInfo();
        response.setDeaneryId(deanery.getId());
        response.setDeaneryName(deanery.getDeaneryName());
        response.setDeanName(deanery.getDean() != null ? deanery.getDean().getName() : null);
        response.setProfessorNames(deanery.getProfessors() != null ?
            deanery.getProfessors().stream().map(Professor::getName).toList() : new ArrayList<>());
        response.setAcademicProgramNames(deanery.getAcademicPrograms() != null ?
            deanery.getAcademicPrograms().stream().map(AcademicProgram::getName).toList() : new ArrayList<>());
        return response;
    }
    
@PostMapping("/deaneries")
@Operation(summary = "Crear decanatura", description = "Crea una nueva decanatura con código y nombre.")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Decanatura creada exitosamente"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o decanatura ya existe"),
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
    @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
})
public ResponseEntity<ManagerResponseDTO.DeaneryInfo> createDeanery(
        @Valid @RequestBody ManagerRequestDTO.DeaneryRequest request,
        HttpSession session) {

    ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
    if (authCheck != null) {
        throw new IllegalArgumentException("No tienes permisos para crear decanaturas");
    }

    if (request.getDeaneryName() == null || request.getDeaneryName().isBlank()) {
        throw new IllegalArgumentException("El nombre de la decanatura es obligatorio");
    }

    Deanery deanery = new Deanery();
    deanery.setDeaneryName(request.getDeaneryName());
    deanery.setId(request.getDeaneryId());
    

    Deanery saved = deaneryService.createDeanery(deanery);
    ManagerResponseDTO.DeaneryInfo response = buildDeaneryResponse(saved);
    return ResponseEntity.status(201).body(response);
}


private ManagerResponseDTO.DeanResponseDTO buildDeanResponse(Dean dean) {
        ManagerResponseDTO.DeanResponseDTO response = new ManagerResponseDTO.DeanResponseDTO();
        response.setDeanCode(dean.getDeanCode());
        response.setName(dean.getName());
        response.setMail(dean.getMail());
        response.setDocument(dean.getDocument());
        return response;
    }

    @PostMapping("/deans")
    @Operation(summary = "Crear decano", description = "Crea un nuevo decano en el sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Decano creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<ManagerResponseDTO.DeanResponseDTO> createDean(
            @Valid @RequestBody ManagerRequestDTO.DeanRequest request,
            HttpSession session) {

            logger.info("Registering new dean: {}", request.getDocument());


        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para crear decanos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());
        userDTO.setType(UserType.DEAN);

        Dean dean = deanService.createDean(userDTO);

        dean.setDeanCode(request.getDeanCode());
        deanService.save(dean);

        ManagerResponseDTO.DeanResponseDTO response = buildDeanResponse(dean);

        logger.info("Dean registered successfully: {}", dean.getDeanCode());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/deans")
    @Operation(
        summary = "Obtener decanos",
        description = "Retorna la lista de todos los decanos registrados en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de decanos obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<List<ManagerResponseDTO.DeanResponseDTO>> getAllDeans(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver decanos");
        }

        List<Dean> deans = deanService.searchAllDeans();
        List<ManagerResponseDTO.DeanResponseDTO> response = deans.stream()
            .map(this::buildDeanResponse)
            .toList();

        return ResponseEntity.ok(response);
    }

    /*
     * Asocia un decano existente a una decanatura.
     * Se requiere que el decano y la decanatura existan en el sistema.
     */
    @PostMapping("/deaneries/{deaneryId}/dean/{deanCode}")
    @Operation(summary = "Asociar decano a decanatura", description = "Asocia un decano existente a una decanatura.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Decano asociado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Decanatura o decano no encontrado"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<ManagerResponseDTO.DeaneryInfo> assignDeanToDeanery(
            @PathVariable String deaneryId,
            @PathVariable String deanCode,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para asociar decanos");
        }

        Deanery deanery = deaneryService.searchDeaneryById(deaneryId);
        Dean dean = deanService.searchDeanByCode(deanCode);

        deanery.setDean(dean);
        deaneryService.modifyDeanery(deanery);

        ManagerResponseDTO.DeaneryInfo response = buildDeaneryResponse(deanery);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deaneries/{deaneryId}/professors/{professorId}")
    @Operation(summary = "Asociar profesor a decanatura", description = "Asocia un profesor existente a una decanatura.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profesor asociado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Decanatura o profesor no encontrado"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<ManagerResponseDTO.DeaneryInfo> addProfessorToDeanery(
            @PathVariable String deaneryId,
            @PathVariable String professorCode,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para asociar profesores");
        }

        Deanery deanery = deaneryService.searchDeaneryById(deaneryId);
        Professor professor = professorService.searchProfessorByCode(professorCode);

        List<Professor> professors = deanery.getProfessors() != null ? deanery.getProfessors() : new ArrayList<>();
        if (!professors.contains(professor)) {
            professors.add(professor);
            deanery.setProfessors(professors);
            deaneryService.modifyDeanery(deanery);
        }

        ManagerResponseDTO.DeaneryInfo response = buildDeaneryResponse(deanery);
        return ResponseEntity.ok(response);
    }





    //------------------------------------------Programas Academicos--------------------------------------------
    
    @PostMapping("/academic-programs")
    @Operation(summary = "Crear programa académico", description = "Crea un nuevo programa académico con código y nombre.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Programa académico creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o programa ya existe"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<ManagerResponseDTO.AcademicProgramInfo> createAcademicProgram(
            @Valid @RequestBody ManagerRequestDTO.AcademicProgramRequest request,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para crear programas académicos");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del programa académico es obligatorio");
        }
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("El ID del programa académico es obligatorio");
        }

        AcademicProgram program = new AcademicProgram();
        program.setId(request.getId());
        program.setName(request.getName());

        AcademicProgram saved = academicProgramService.createProgram(program);
        ManagerResponseDTO.AcademicProgramInfo response = buildAcademicProgramResponse(saved);

        return ResponseEntity.status(201).body(response);
        
    }

    private ManagerResponseDTO.AcademicProgramInfo buildAcademicProgramResponse(AcademicProgram program) {
        ManagerResponseDTO.AcademicProgramInfo response = new ManagerResponseDTO.AcademicProgramInfo();
        response.setId(program.getId());
        response.setName(program.getName());
        response.setDeaneryName(program.getDeanery() != null ? program.getDeanery().getDeaneryName() : null);
        return response;
    }

    @PostMapping("/deaneries/{deaneryId}/programs/{programId}")
    @Operation(summary = "Asociar programa académico a decanatura", description = "Asocia un programa académico existente a una decanatura.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Programa académico asociado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Decanatura o programa no encontrado"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<ManagerResponseDTO.DeaneryInfo> addProgramToDeanery(
            @PathVariable String deaneryId,
            @PathVariable String programId,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para asociar programas");
        }

        Deanery deanery = deaneryService.searchDeaneryById(deaneryId);
        AcademicProgram program = academicProgramService.searchProgramById(programId);

        List<AcademicProgram> programs = deanery.getAcademicPrograms() != null ? deanery.getAcademicPrograms() : new ArrayList<>();
        if (!programs.contains(program)) {
            programs.add(program);
            deanery.setAcademicPrograms(programs);
            deaneryService.modifyDeanery(deanery);
        }

        ManagerResponseDTO.DeaneryInfo response = buildDeaneryResponse(deanery);
        return ResponseEntity.ok(response);
    }
}