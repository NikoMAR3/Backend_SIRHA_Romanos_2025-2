package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.*;
import edu.dosw.sirha.controller.utils.GlobalExceptionHandler.ErrorResponse;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.*;
import edu.dosw.sirha.model.components.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for managing student operations and functionalities.
 * Provides endpoints for student authentication, schedule management,
 * academic status monitoring, and petition handling.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students Management", description = "Endpoints para gestión de estudiantes")
public class StudentsController {

    private static final Logger logger = LoggerFactory.getLogger(StudentsController.class);

    private final StudentService studentService;
    private final TrafficLightService trafficLightService;
    private final ScheduleService scheduleService;
    private final AcademicProgramService academicProgramService;
    private final DeaneryService deaneryService;
    private final PetitionService petitionService;
    private final List<PetitionCreator> petitionCreators;
    private final AuthenticationService authenticationService;

    /**
     * Authenticates a student with institutional credentials - Public endpoint.
     */
    @Operation(
            summary = "Autenticación de estudiante",
            description = "Autentica un estudiante con credenciales institucionales"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "No es un estudiante")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> authenticateStudent(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpSession session) {

        logger.info("Student authentication attempt for credential: {}", request.getCredential());

        AuthenticationService.AuthenticationResult result =
                authenticationService.authenticate(request.getCredential(), request.getPassword());

        if (result.isSuccess()) {
            if (result.getUser().getType() != UserType.STUDENT) {
                logger.warn("Non-student user {} attempted to access student login", result.getUser().getId());
                throw new IllegalArgumentException("Solo estudiantes pueden acceder a esta funcionalidad");
            }

            session.setAttribute("user", result.getUser());
            session.setAttribute("userId", result.getUser().getId());
            session.setAttribute("userType", result.getUser().getType());

            logger.info("Student {} authenticated successfully", result.getUser().getId());
            return ResponseEntity.ok(new AuthDto.LoginResponse(true, "Autenticación exitosa", result.getUser()));
        } else {
            logger.warn("Failed authentication attempt for credential: {}", request.getCredential());
            throw new IllegalArgumentException("Credenciales inválidas: " + result.getMessage());
        }
    }

    /**
     * Registers a new student in the system - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @Operation(
            summary = "Registrar nuevo estudiante",
            description = "Crea un nuevo estudiante en el sistema con credenciales institucionales"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estudiante creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "409", description = "Estudiante ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<StudentsResponseDTO> registerStudent(
            @Valid @RequestBody StudentsRequestDTO request,
            HttpSession session) {

        logger.info("Registering new student: {}", request.getDocument());

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para registrar estudiantes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());
        userDTO.setType(UserType.STUDENT);

        Student student = studentService.createStudent(userDTO);
        StudentsResponseDTO response = buildStudentResponse(student);

        logger.info("Student created successfully with ID: {} by user: {}", student.getId(), currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves complete information for a specific student - Students can only see their own, Deans/VP can see any.
     */
    @Operation(
            summary = "Obtener información del estudiante",
            description = "Consulta la información completa de un estudiante incluyendo horarios y semáforo académico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta información"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentsResponseDTO> getStudentInfo(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Retrieving student info for ID: {}", studentId);

        validateStudentAccess(studentId, session);

        Student student = studentService.searchStudentById(studentId);
        StudentsResponseDTO response = buildStudentResponse(student);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the current semester schedule for a student - Students can only see their own, Deans/VP can see any.
     */
    @Operation(
            summary = "Consultar horario actual",
            description = "Obtiene el horario del semestre actual del estudiante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horario obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este horario"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    @GetMapping("/{studentId}/schedule/current")
    public ResponseEntity<List<Map<String, Object>>> getCurrentSchedule(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Retrieving current schedule for student: {}", studentId);

        validateStudentAccess(studentId, session);

        Schedule schedule = studentService.getStudentSchedule(studentId);
        List<Map<String, Object>> currentSchedule = buildScheduleResponse(schedule);

        return ResponseEntity.ok(currentSchedule);
    }

    /**
     * Retrieves the historical schedules for previous semesters - Students can only see their own, Deans/VP can see any.
     */
    @Operation(
            summary = "Consultar historial de horarios",
            description = "Obtiene los horarios de semestres anteriores del estudiante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de horarios obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este historial"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    @GetMapping("/{studentId}/schedule/history")
    public ResponseEntity<List<Map<String, Object>>> getScheduleHistory(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Retrieving schedule history for student: {}", studentId);

        validateStudentAccess(studentId, session);

        List<Schedule> historySchedules = scheduleService.getScheduleHistory(studentId);
        List<Map<String, Object>> response = historySchedules.stream()
                .map(this::buildScheduleResponse)
                .flatMap(List::stream)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the academic traffic light status for a student - Students can only see their own, Deans/VP can see any.
     */
    @Operation(
            summary = "Consultar semáforo académico",
            description = "Obtiene el estado del semáforo académico del estudiante (verde=normal, azul=en progreso, rojo=perdida)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Semáforo académico obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este semáforo"),
            @ApiResponse(responseCode = "404", description = "Estudiante o semáforo no encontrado")
    })
    @GetMapping("/{studentId}/traffic-light")
    public ResponseEntity<Map<String, Object>> getTrafficLight(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Retrieving traffic light for student: {}", studentId);

        validateStudentAccess(studentId, session);

        Optional<TrafficLight> trafficLightOpt = trafficLightService.searchTrafficLightByStudentId(studentId);

        if (trafficLightOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TrafficLight trafficLight = trafficLightOpt.get();
        Map<String, Object> response = buildTrafficLightResponse(trafficLight);

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new petition for subject/group changes - STUDENT only (for themselves).
     */
    @Operation(
            summary = "Crear solicitud de cambio",
            description = "Crea una nueva solicitud de cambio de materia/grupo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Solo puedes crear solicitudes para ti mismo")
    })
    @PostMapping("/{studentId}/petitions")
    public ResponseEntity<PetitionResponseDTO> createPetition(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            @Valid @RequestBody PetitionRequestDTO request,
            HttpSession session) {

        logger.info("Creating petition for student: {} of type: {}", studentId, request.getType());

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.STUDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo estudiantes pueden crear solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        
        if (!currentUser.getId().equals(studentId)) {
            logger.warn("Student {} attempted to create petition for student {}", currentUser.getId(), studentId);
            throw new IllegalArgumentException("Solo puedes crear solicitudes para ti mismo");
        }

        PetitionCreator appropriateCreator = petitionCreators.stream()
                .filter(creator -> creator.supports(request.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un creator para el tipo de petición: " + request.getType()));

        Petition petition = appropriateCreator.createPetition(request);
        petition.setStudentId(studentId);

        Petition createdPetition = petitionService.createPetition(petition);
        PetitionResponseDTO response = buildPetitionResponse(createdPetition);

        logger.info("Petition created successfully with ID: {} for student: {}", 
                   createdPetition.getPetitionId(), studentId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the current status of a specific petition - STUDENT only (their own petitions).
     */
    @Operation(
            summary = "Consultar estado de solicitud",
            description = "Obtiene el estado actual de una solicitud específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de solicitud obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta solicitud"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @GetMapping("/{studentId}/petitions/{petitionId}")
    public ResponseEntity<PetitionResponseDTO> getPetitionStatus(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            @Parameter(description = "ID de la solicitud", required = true)
            @PathVariable String petitionId,
            HttpSession session) {

        logger.debug("Retrieving petition status: {} for student: {}", petitionId, studentId);

     
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.STUDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo estudiantes pueden consultar sus solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

     
        if (!currentUser.getId().equals(studentId)) {
            throw new IllegalArgumentException("Solo puedes consultar tus propias solicitudes");
        }

        Petition petition = petitionService.searchPetitionsById(petitionId);

    
        if (!petition.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("No tienes permisos para ver esta solicitud");
        }

        PetitionResponseDTO response = buildPetitionResponse(petition);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the complete petition history for a student - STUDENT only (their own history).
     */
    @Operation(
            summary = "Historial de solicitudes",
            description = "Obtiene todas las solicitudes realizadas por el estudiante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de solicitudes obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Solo puedes ver tu propio historial")
    })
    @GetMapping("/{studentId}/petitions")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionHistory(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Retrieving petition history for student: {}", studentId);

       
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.STUDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo estudiantes pueden consultar su historial");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        
        if (!currentUser.getId().equals(studentId)) {
            throw new IllegalArgumentException("Solo puedes ver tu propio historial de solicitudes");
        }

        List<Petition> petitions = petitionService.searchPetitionsByStudentId(studentId);
        List<PetitionResponseDTO> response = petitions.stream()
                .map(this::buildPetitionResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves petitions filtered by their current state - STUDENT only (their own petitions).
     */
    @Operation(
            summary = "Consultar solicitudes por estado",
            description = "Obtiene las solicitudes del estudiante filtradas por estado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitudes filtradas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Solo puedes filtrar tus propias solicitudes")
    })
    @GetMapping("/{studentId}/petitions/by-state")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByState(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            @Parameter(description = "Estado de la solicitud", required = true)
            @RequestParam PetitionState state,
            HttpSession session) {

        logger.debug("Retrieving petitions by state {} for student: {}", state, studentId);

       
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.STUDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo estudiantes pueden filtrar sus solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

    
        if (!currentUser.getId().equals(studentId)) {
            throw new IllegalArgumentException("Solo puedes filtrar tus propias solicitudes");
        }

        List<Petition> allPetitions = petitionService.searchPetitionsByStudentId(studentId);
        List<Petition> filteredPetitions = allPetitions.stream()
                .filter(petition -> petition.getState() == state)
                .toList();

        List<PetitionResponseDTO> response = filteredPetitions.stream()
                .map(this::buildPetitionResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Calculates and returns the student's Grade Point Average (GPA) - Students can only see their own, Deans/VP can see any.
     */
    @Operation(summary = "Calcular GPA del estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GPA calculado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este GPA"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    @GetMapping("/{studentId}/gpa")
    public ResponseEntity<Map<String, Object>> calculateGPA(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Calculating GPA for student: {}", studentId);

        validateStudentAccess(studentId, session);

        double gpa = studentService.calculateGPA(studentId);

        Map<String, Object> response = Map.of(
                "studentId", studentId,
                "gpa", gpa,
                "calculatedAt", LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the personal information of a student - STUDENT only (their own info), or DEAN/VP.
     */
    @Operation(summary = "Actualizar información del estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información actualizada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta información"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    @PutMapping("/{studentId}")
    public ResponseEntity<StudentsResponseDTO> updateStudent(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            @Valid @RequestBody StudentsRequestDTO request,
            HttpSession session) {

        logger.info("Updating student information for ID: {}", studentId);

        validateStudentAccess(studentId, session);

        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());
        userDTO.setType(UserType.STUDENT);

        Optional<Student> updatedStudent = studentService.modifyStudent(studentId, userDTO);

        if (updatedStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentsResponseDTO response = buildStudentResponse(updatedStudent.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Enrolls a student in a specific course - STUDENT only (for themselves), or DEAN/VP.
     */
    @Operation(summary = "Inscribir materia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscripción exitosa"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para inscribir a este estudiante"),
            @ApiResponse(responseCode = "404", description = "Estudiante o materia no encontrada")
    })
    @PostMapping("/{studentId}/enroll/{courseId}")
    public ResponseEntity<Map<String, String>> enrollInCourse(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            @Parameter(description = "ID de la materia", required = true)
            @PathVariable String courseId,
            HttpSession session) {

        logger.info("Enrolling student {} in course {}", studentId, courseId);

        validateStudentAccess(studentId, session);

        studentService.enrollInCourse(studentId, courseId);

        Map<String, String> response = Map.of(
                "message", "Estudiante inscrito exitosamente en la materia",
                "studentId", studentId,
                "courseId", courseId,
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Withdraws a student from a specific course - STUDENT only (for themselves), or DEAN/VP.
     */
    @Operation(summary = "Retirar materia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retiro exitoso"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para retirar a este estudiante"),
            @ApiResponse(responseCode = "404", description = "Estudiante o materia no encontrada")
    })
    @DeleteMapping("/{studentId}/withdraw/{courseId}")
    public ResponseEntity<Map<String, String>> withdrawFromCourse(
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            @Parameter(description = "ID de la materia", required = true)
            @PathVariable String courseId,
            HttpSession session) {

        logger.info("Withdrawing student {} from course {}", studentId, courseId);

        validateStudentAccess(studentId, session);

        studentService.withdrawFromCourse(studentId, courseId);

        Map<String, String> response = Map.of(
                "message", "Estudiante retirado exitosamente de la materia",
                "studentId", studentId,
                "courseId", courseId,
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(response);
    }

   

    /**
     * Validates that the authenticated user has access to the specified student data.
     * Students can only access their own data, while Deans and Academic VP can access any student.
     */
    private void validateStudentAccess(String studentId, HttpSession session) {
     
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        switch (currentUser.getType()) {
            case STUDENT:
               
                if (!currentUser.getId().equals(studentId)) {
                    logger.warn("Student {} attempted to access data for student {}", 
                               currentUser.getId(), studentId);
                    throw new IllegalArgumentException("No tienes permisos para acceder a la información de este estudiante");
                }
                break;
            case DEAN:
            case ACADEMIC_VICEPRESIDENT:
                
                logger.debug("Admin user {} accessing student data for: {}", currentUser.getId(), studentId);
                break;
            case PROFESSOR:
        
                throw new IllegalArgumentException("Los profesores no tienen permisos para acceder a información detallada de estudiantes");
            default:
                throw new IllegalArgumentException("Tipo de usuario no válido para esta operación");
        }
    }



    /**
     * Builds a complete student response DTO with all relevant information.
     */
    private StudentsResponseDTO buildStudentResponse(Student student) {
        StudentsResponseDTO response = new StudentsResponseDTO();
        response.setName(student.getName());
        response.setMail(student.getMail());
        response.setDocument(student.getDocument());
        response.setAcademicStatus(student.getAcademicStatus());
        response.setRegistrationDate(student.getCreatedAt());
        response.setLastUpdateDate(LocalDateTime.now());

        if (student.getAcademicProgram() != null) {
            Map<String, Object> programInfo = Map.of(
                    "id", student.getAcademicProgram().getId(),
                    "name", student.getAcademicProgram().getName()
            );
            response.setAcademicProgram(programInfo);
        }

        if (student.getDeanery() != null) {
            Map<String, Object> deaneryInfo = Map.of(
                    "id", student.getDeanery().getId(),
                    "name", student.getDeanery().getDeaneryName()
            );
            response.setDeanery(deaneryInfo);
        }

        if (student.getTrafficLight() != null) {
            response.setTrafficLight(buildTrafficLightResponse(student.getTrafficLight()));
        }

        response.setAvailableActions(Arrays.asList(
                "VIEW_SCHEDULE", "CHECK_GRADES", "UPDATE_PROFILE", "CREATE_PETITION"
        ));

        return response;
    }

    /**
     * Builds a petition response DTO from a petition entity.
     */
    private PetitionResponseDTO buildPetitionResponse(Petition petition) {
        PetitionResponseDTO response = new PetitionResponseDTO();
        response.setPetitionId(petition.getPetitionId());
        response.setStudentId(petition.getStudentId());
        response.setType(petition.getType());
        response.setSubjectId(petition.getSubjectId());
        response.setAssociateDeanery(petition.getAssociateDeanery());
        response.setPriority(petition.getPriority());
        response.setState(petition.getState());
        response.setCreationDate(petition.getCreationDate());
        response.setModificationDate(petition.getModificationDate());
        response.setJustification(petition.getJustification());
        response.setAssignedReviewer(petition.getAssignedReviewer());
        response.setRejectionReason(petition.getRejectionReason());

        response.setQueuePosition(generateQueuePosition(petition));

        return response;
    }

    /**
     * Generates an estimated queue position for a petition based on creation date
     * and other pending petitions.
     */
    private Integer generateQueuePosition(Petition petition) {
        List<Petition> pendingPetitions = petitionService.searchPetitionsByState(PetitionState.PENDING);
        long earlierPetitions = pendingPetitions.stream()
                .filter(p -> p.getCreationDate().isBefore(petition.getCreationDate()))
                .count();
        return (int) earlierPetitions + 1;
    }

    /**
     * Builds a traffic light response map with academic status information.
     */
    private Map<String, Object> buildTrafficLightResponse(TrafficLight trafficLight) {
        return Map.of(
                "status", trafficLight.getStatus().name(),
                "description", trafficLight.getStatus().getDescription(),
                "grade", trafficLight.getGrade(),
                "credits", trafficLight.getCredits(),
                "semester", trafficLight.getSemester(),
                "approvedSubjects", trafficLight.getApprovedSubjects().size(),
                "failedSubjects", trafficLight.getFailedSubjects().size(),
                "ongoingSubjects", trafficLight.getOnGoingSubjects().size()
        );
    }

    /**
     * Builds a schedule response list from a schedule entity.
     */
    private List<Map<String, Object>> buildScheduleResponse(Schedule schedule) {
        if (schedule.getSubjects() == null) {
            return Collections.emptyList();
        }

        return schedule.getSubjects().stream()
                .map(subject -> Map.<String, Object>of(
                        "subjectId", subject.getId(),
                        "subjectName", subject.getName(),
                        "credits", subject.getCredits(),
                        "classroom", schedule.getClassroom(),
                        "dayOfWeek", schedule.getDayOfWeek(),
                        "startTime", schedule.getStartTime(),
                        "endTime", schedule.getEndTime()
                ))
                .toList();
    }
}