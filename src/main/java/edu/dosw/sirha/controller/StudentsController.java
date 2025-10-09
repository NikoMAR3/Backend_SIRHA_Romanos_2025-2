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

    private final StudentService studentService;
    private final TrafficLightService trafficLightService;
    private final ScheduleService scheduleService;
    private final AcademicProgramService academicProgramService;
    private final DeaneryService deaneryService;
    private final PetitionService petitionService;
    private final List<PetitionCreator> petitionCreators;
    private final AuthenticationService authenticationService;

    /**
     * Authenticates a student with institutional credentials.
     *
     * @param request the login request containing credentials
     * @param session HTTP session for storing authentication data
     * @return authentication response with user information
     * @throws IllegalArgumentException if credentials are invalid or user is not a student
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

        AuthenticationService.AuthenticationResult result =
                authenticationService.authenticate(request.getCredential(), request.getPassword());

        if (result.isSuccess()) {
            if (result.getUser().getType() != UserType.STUDENT) {
                throw new IllegalArgumentException("Solo estudiantes pueden acceder a esta funcionalidad");
            }

            session.setAttribute("user", result.getUser());
            session.setAttribute("userId", result.getUser().getId());
            session.setAttribute("userType", result.getUser().getType());

            return ResponseEntity.ok(new AuthDto.LoginResponse(true, "Autenticación exitosa", result.getUser()));
        } else {
            throw new IllegalArgumentException("Credenciales inválidas: " + result.getMessage());
        }
    }

    /**
     * Registers a new student in the system with institutional credentials.
     *
     * @param request the student registration request
     * @return the created student information
     */
    @Operation(
            summary = "Registrar nuevo estudiante",
            description = "Crea un nuevo estudiante en el sistema con credenciales institucionales"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estudiante creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Estudiante ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<StudentsResponseDTO> registerStudent(
            @Valid @RequestBody StudentsRequestDTO request) {

        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());
        userDTO.setType(UserType.STUDENT);

        Student student = studentService.createStudent(userDTO);
        StudentsResponseDTO response = buildStudentResponse(student);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves complete information for a specific student including schedules and academic status.
     *
     * @param studentId the ID of the student to retrieve
     * @param session HTTP session for authentication validation
     * @return complete student information
     */
    @Operation(
            summary = "Obtener información del estudiante",
            description = "Consulta la información completa de un estudiante incluyendo horarios y semáforo académico"
    )
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentsResponseDTO> getStudentInfo(
            @PathVariable String studentId,
            HttpSession session) {

        validateStudentAccess(studentId, session);

        Student student = studentService.searchStudentById(studentId);
        StudentsResponseDTO response = buildStudentResponse(student);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the current semester schedule for a student.
     *
     * @param studentId the ID of the student
     * @param session HTTP session for authentication validation
     * @return current schedule details
     */
    @Operation(
            summary = "Consultar horario actual",
            description = "Obtiene el horario del semestre actual del estudiante"
    )
    @GetMapping("/{studentId}/schedule/current")
    public ResponseEntity<List<Map<String, Object>>> getCurrentSchedule(
            @PathVariable String studentId,
            HttpSession session) {

        validateStudentAccess(studentId, session);

        Schedule schedule = studentService.getStudentSchedule(studentId);
        List<Map<String, Object>> currentSchedule = buildScheduleResponse(schedule);

        return ResponseEntity.ok(currentSchedule);
    }

    /**
     * Retrieves the historical schedules for previous semesters.
     *
     * @param studentId the ID of the student
     * @param session HTTP session for authentication validation
     * @return list of historical schedules
     */
    @Operation(
            summary = "Consultar historial de horarios",
            description = "Obtiene los horarios de semestres anteriores del estudiante"
    )
    @GetMapping("/{studentId}/schedule/history")
    public ResponseEntity<List<Map<String, Object>>> getScheduleHistory(
            @PathVariable String studentId,
            HttpSession session) {

        validateStudentAccess(studentId, session);

        List<Schedule> historySchedules = scheduleService.getScheduleHistory(studentId);
        List<Map<String, Object>> response = historySchedules.stream()
                .map(this::buildScheduleResponse)
                .flatMap(List::stream)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the academic traffic light status for a student.
     * Traffic light indicates: green (normal), blue (in progress), red (failing).
     *
     * @param studentId the ID of the student
     * @param session HTTP session for authentication validation
     * @return traffic light status information
     */
    @Operation(
            summary = "Consultar semáforo académico",
            description = "Obtiene el estado del semáforo académico del estudiante (verde=normal, azul=en progreso, rojo=perdida)"
    )
    @GetMapping("/{studentId}/traffic-light")
    public ResponseEntity<Map<String, Object>> getTrafficLight(
            @PathVariable String studentId,
            HttpSession session) {

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
     * Creates a new petition for subject/group changes.
     *
     * @param studentId the ID of the student creating the petition
     * @param request the petition request details
     * @param session HTTP session for authentication validation
     * @return the created petition information
     */
    @Operation(
            summary = "Crear solicitud de cambio",
            description = "Crea una nueva solicitud de cambio de materia/grupo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/{studentId}/petitions")
    public ResponseEntity<PetitionResponseDTO> createPetition(
            @PathVariable String studentId,
            @Valid @RequestBody PetitionRequestDTO request,
            HttpSession session) {

        validateStudentAccess(studentId, session);

        PetitionCreator appropriateCreator = petitionCreators.stream()
                .filter(creator -> creator.supports(request.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un creator para el tipo de petición: " + request.getType()));

        Petition petition = appropriateCreator.createPetition(request);

        petition.setStudentId(studentId);

        Petition createdPetition = petitionService.createPetition(petition);
        PetitionResponseDTO response = buildPetitionResponse(createdPetition);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the current status of a specific petition.
     *
     * @param studentId the ID of the student
     * @param petitionId the ID of the petition to check
     * @param session HTTP session for authentication validation
     * @return petition status information
     */
    @Operation(
            summary = "Consultar estado de solicitud",
            description = "Obtiene el estado actual de una solicitud específica"
    )
    @GetMapping("/{studentId}/petitions/{petitionId}")
    public ResponseEntity<PetitionResponseDTO> getPetitionStatus(
            @PathVariable String studentId,
            @PathVariable String petitionId,
            HttpSession session) {

        validateStudentAccess(studentId, session);

        Petition petition = petitionService.searchPetitionsById(petitionId);

        if (!petition.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("No tienes permisos para ver esta solicitud");
        }

        PetitionResponseDTO response = buildPetitionResponse(petition);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the complete petition history for a student.
     *
     * @param studentId the ID of the student
     * @param session HTTP session for authentication validation
     * @return list of all petitions made by the student
     */
    @Operation(
            summary = "Historial de solicitudes",
            description = "Obtiene todas las solicitudes realizadas por el estudiante"
    )
    @GetMapping("/{studentId}/petitions")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionHistory(
            @PathVariable String studentId,
            HttpSession session) {

        validateStudentAccess(studentId, session);

        List<Petition> petitions = petitionService.searchPetitionsByStudentId(studentId);
        List<PetitionResponseDTO> response = petitions.stream()
                .map(this::buildPetitionResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves petitions filtered by their current state.
     *
     * @param studentId the ID of the student
     * @param state the petition state to filter by
     * @param session HTTP session for authentication validation
     * @return list of petitions with the specified state
     */
    @Operation(
            summary = "Consultar solicitudes por estado",
            description = "Obtiene las solicitudes del estudiante filtradas por estado"
    )
    @GetMapping("/{studentId}/petitions/by-state")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByState(
            @PathVariable String studentId,
            @RequestParam PetitionState state,
            HttpSession session) {

        validateStudentAccess(studentId, session);

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
     * Calculates and returns the student's Grade Point Average (GPA).
     *
     * @param studentId the ID of the student
     * @param session HTTP session for authentication validation
     * @return GPA calculation result
     */
    @Operation(summary = "Calcular GPA del estudiante")
    @GetMapping("/{studentId}/gpa")
    public ResponseEntity<Map<String, Object>> calculateGPA(
            @PathVariable String studentId,
            HttpSession session) {

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
     * Updates the personal information of a student.
     *
     * @param studentId the ID of the student to update
     * @param request the update request with new information
     * @param session HTTP session for authentication validation
     * @return updated student information
     */
    @Operation(summary = "Actualizar información del estudiante")
    @PutMapping("/{studentId}")
    public ResponseEntity<StudentsResponseDTO> updateStudent(
            @PathVariable String studentId,
            @Valid @RequestBody StudentsRequestDTO request,
            HttpSession session) {

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
     * Enrolls a student in a specific course.
     *
     * @param studentId the ID of the student
     * @param courseId the ID of the course to enroll in
     * @param session HTTP session for authentication validation
     * @return enrollment confirmation
     */
    @Operation(summary = "Inscribir materia")
    @PostMapping("/{studentId}/enroll/{courseId}")
    public ResponseEntity<Map<String, String>> enrollInCourse(
            @PathVariable String studentId,
            @PathVariable String courseId,
            HttpSession session) {

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
     * Withdraws a student from a specific course.
     *
     * @param studentId the ID of the student
     * @param courseId the ID of the course to withdraw from
     * @param session HTTP session for authentication validation
     * @return withdrawal confirmation
     */
    @Operation(summary = "Retirar materia")
    @DeleteMapping("/{studentId}/withdraw/{courseId}")
    public ResponseEntity<Map<String, String>> withdrawFromCourse(
            @PathVariable String studentId,
            @PathVariable String courseId,
            HttpSession session) {

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
     * Ensures the user is authenticated, is a student, and can only access their own data.
     *
     * @param studentId the ID of the student to validate access for
     * @param session HTTP session containing authentication information
     * @throws IllegalArgumentException if access is not authorized
     */
    private void validateStudentAccess(String studentId, HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        if (currentUser.getType() != UserType.STUDENT) {
            throw new IllegalArgumentException("Solo estudiantes pueden acceder a esta funcionalidad");
        }

        if (!currentUser.getId().equals(studentId)) {
            throw new IllegalArgumentException("No tienes permisos para acceder a la información de este estudiante");
        }
    }

    /**
     * Builds a complete student response DTO with all relevant information.
     *
     * @param student the student entity to convert
     * @return formatted student response DTO
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

        return response;
    }

    /**
     * Builds a petition response DTO from a petition entity.
     *
     * @param petition the petition entity to convert
     * @return formatted petition response DTO
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
     *
     * @param petition the petition to calculate position for
     * @return estimated position in the processing queue
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
     *
     * @param trafficLight the traffic light entity containing academic status
     * @return formatted traffic light information
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
     *
     * @param schedule the schedule entity to convert
     * @return formatted list of schedule information
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
