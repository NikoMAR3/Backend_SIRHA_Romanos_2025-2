package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.controller.dtos.GroupsResponseDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.ClassSessionService;
import edu.dosw.sirha.model.services.SubjectService;
import edu.dosw.sirha.model.services.ProfessorService;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing group operations and functionalities.
 * Provides endpoints for group creation, capacity management, professor assignments,
 * schedule administration, and student enrollment for the SIRHA system.
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups Management", description = "Endpoints para gestión completa de grupos académicos")
public class GroupsController {

    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);

    private final ClassSessionService classSessionService;
    private final SubjectService subjectService;
    private final ProfessorService professorService;

    /**
     * Creates a new academic group - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @PostMapping
    @Operation(
            summary = "Crear nuevo grupo académico",
            description = "Crea un nuevo grupo académico con validación automática de capacidad y detección de conflictos de horario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de grupo inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "409", description = "Conflicto de horarios o capacidad excedida")
    })
    public ResponseEntity<GroupsResponseDTO> createGroup(
            @Valid @RequestBody GroupsRequestDTO request,
            HttpSession session) {

        logger.info("Creating new group: {} for subject: {}", 
                   request.getGroupName(), request.getSubjectId());

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para crear grupos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        logger.info("User {} creating group for subject: {}", currentUser.getId(), request.getSubjectId());

        ClassSession sessionEntity = createSessionFromRequest(request);
        ClassSession savedSession = classSessionService.createClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(savedSession);

        logger.info("Group created successfully with ID: {} by user: {}", savedSession.getId(), currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves detailed information about a specific group - All authenticated users.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener información del grupo",
            description = "Obtiene información detallada de un grupo específico incluyendo estadísticas de inscripción"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información del grupo obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> getGroupById(
            @Parameter(description = "Identificador único del grupo", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.debug("Retrieving group with ID: {}", id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(id);
        GroupsResponseDTO response = convertToResponseDTO(sessionEntity);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all academic groups - All authenticated users.
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los grupos",
            description = "Obtiene todos los grupos académicos con su estado actual de capacidad e inscripciones"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de grupos obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<List<GroupsResponseDTO>> getAllGroups(HttpSession session) {
        logger.debug("Retrieving all groups");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> sessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> response = sessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} groups", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates group information - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar información del grupo",
            description = "Actualiza la información del grupo con validación de límites de capacidad y conflictos de horario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de grupo inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> updateGroup(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Valid @RequestBody GroupsRequestDTO request,
            HttpSession session) {

        logger.info("Updating group with ID: {}", id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para actualizar grupos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        ClassSession existingSession = classSessionService.searchSessionById(id);
        updateSessionFromRequest(existingSession, request);
        ClassSession updatedSession = classSessionService.updateClassSession(existingSession);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        logger.info("Group updated successfully with ID: {} by user: {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a group - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar grupo",
            description = "Elimina un grupo después de validar que no existen inscripciones activas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grupo eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar grupo con inscripciones activas"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID del grupo a eliminar", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.info("Deleting group with ID: {}", id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para eliminar grupos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        try {
            classSessionService.deleteClassSession(id);
            logger.info("Group deleted successfully with ID: {} by user: {}", id, currentUser.getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.warn("Group not found for deletion with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves detailed capacity information - All authenticated users.
     */
    @GetMapping("/{id}/capacity")
    @Operation(
            summary = "Consultar información de capacidad",
            description = "Obtiene información detallada de capacidad incluyendo estudiantes inscritos y porcentaje de ocupación"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información de capacidad obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<Map<String, Object>> getGroupCapacity(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.debug("Retrieving capacity information for group: {}", id);

    
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        ClassSessionService.EnrollmentStats stats = classSessionService.getEnrollmentStats(id);
        
        Map<String, Object> capacityInfo = Map.of(
            "groupId", id,
            "maxStudents", stats.getCapacity(),
            "currentStudents", stats.getEnrolled(),
            "availableSpots", stats.getAvailable(),
            "waitingListCount", stats.getWaitingList(),
            "occupancyPercentage", stats.getOccupancyPercentage(),
            "isFull", stats.getEnrolled() >= stats.getCapacity(),
            "isNearCapacity", stats.getOccupancyPercentage() >= 90.0
        );

        return ResponseEntity.ok(capacityInfo);
    }

    /**
     * Assigns a professor to a group - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @PutMapping("/{id}/professor")
    @Operation(
            summary = "Asignar profesor al grupo",
            description = "Asigna un profesor a un grupo con validación de conflictos de horario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profesor asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Conflicto de horarios o profesor no válido"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Grupo o profesor no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> assignProfessor(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Parameter(description = "ID del profesor a asignar", required = true)
            @RequestParam String professorId,
            HttpSession session) {

        logger.info("Assigning professor {} to group {}", professorId, id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para asignar profesores a grupos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        ClassSession sessionEntity = classSessionService.searchSessionById(id);
        sessionEntity.setProfessorId(professorId);
        ClassSession updatedSession = classSessionService.updateClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        logger.info("Professor assigned successfully to group: {} by user: {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Enrolls a student in a group - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @PostMapping("/{id}/students/{studentId}")
    @Operation(
            summary = "Inscribir estudiante en grupo",
            description = "Inscribe un estudiante en un grupo con validación de capacidad y conflictos de horario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudiante inscrito exitosamente"),
            @ApiResponse(responseCode = "400", description = "Capacidad excedida o conflicto de horarios"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Grupo o estudiante no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> enrollStudent(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.info("Enrolling student {} in group {}", studentId, id);


        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para inscribir estudiantes en grupos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        ClassSession sessionEntity = classSessionService.enrollStudent(id, studentId);
        GroupsResponseDTO response = convertToResponseDTO(sessionEntity);

        logger.info("Student enrolled successfully in group: {} by user: {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraws a student from a group - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @DeleteMapping("/{id}/students/{studentId}")
    @Operation(
            summary = "Retirar estudiante del grupo",
            description = "Retira un estudiante del grupo e inscribe automáticamente al siguiente estudiante de la lista de espera"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudiante retirado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Estudiante no está inscrito en el grupo"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Grupo o estudiante no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> withdrawStudent(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.info("Withdrawing student {} from group {}", studentId, id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para retirar estudiantes de grupos");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        ClassSession sessionEntity = classSessionService.withdrawStudent(id, studentId);
        GroupsResponseDTO response = convertToResponseDTO(sessionEntity);

        logger.info("Student withdrawn successfully from group: {} by user: {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves list of students enrolled in a specific group - All authenticated users can see enrollments.
     */
    @GetMapping("/{id}/students")
    @Operation(
            summary = "Estudiantes inscritos",
            description = "Obtiene la lista de estudiantes inscritos en un grupo específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estudiantes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<List<String>> getEnrolledStudents(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.debug("Retrieving enrolled students for group: {}", id);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(id);
        List<String> enrolledStudents = sessionEntity.getEnrolledStudentIds();
        
        logger.info("Retrieved {} enrolled students for group: {}", 
                   enrolledStudents != null ? enrolledStudents.size() : 0, id);
        return ResponseEntity.ok(enrolledStudents != null ? enrolledStudents : List.of());
    }

    /**
     * Gets students on waiting list - DEAN and ACADEMIC_VICEPRESIDENT can see waiting lists.
     */
    @GetMapping("/{id}/waiting-list")
    @Operation(
            summary = "Lista de espera",
            description = "Obtiene la lista de estudiantes en lista de espera para un grupo específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de espera obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<List<String>> getWaitingList(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.debug("Retrieving waiting list for group: {}", id);


        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver listas de espera");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(id);
        List<String> waitingList = sessionEntity.getWaitingListStudentIds();
        
        return ResponseEntity.ok(waitingList != null ? waitingList : List.of());
    }

    /**
     * Retrieves all schedule information - All authenticated users.
     */
    @GetMapping("/{id}/schedules")
    @Operation(
            summary = "Horarios del grupo",
            description = "Obtiene toda la información de horarios para un grupo específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horarios obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<List<Map<String, String>>> getGroupSchedules(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.debug("Retrieving schedules for group: {}", id);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSchedule> schedules = classSessionService.searchSchedulesBySession(id);
        
        List<Map<String, String>> scheduleInfo = schedules.stream()
                .map(schedule -> Map.of(
                    "id", schedule.getId(),
                    "dayOfWeek", schedule.getDayOfWeek(),
                    "startTime", schedule.getStartTime().toString(),
                    "endTime", schedule.getEndTime().toString(),
                    "classroom", schedule.getClassroom()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(scheduleInfo);
    }


    /**
     * Retrieves groups near capacity - All authenticated users.
     */
    @GetMapping("/reports/near-capacity")
    @Operation(
            summary = "Grupos cerca del límite de capacidad",
            description = "Obtiene todos los grupos que están al 90% o más de su capacidad para propósitos de monitoreo"
    )
    public ResponseEntity<List<GroupsResponseDTO>> getGroupsNearCapacity(HttpSession session) {
        logger.debug("Retrieving groups near capacity");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> nearCapacityGroups = allSessions.stream()
                .map(this::convertToResponseDTO)
                .filter(group -> group.getOccupancyPercentage() >= 90.0)
                .collect(Collectors.toList());

        logger.info("Found {} groups near capacity", nearCapacityGroups.size());
        return ResponseEntity.ok(nearCapacityGroups);
    }

    /**
     * Retrieves full groups - All authenticated users.
     */
    @GetMapping("/reports/full")
    @Operation(
            summary = "Grupos con capacidad completa",
            description = "Obtiene todos los grupos que han alcanzado su capacidad máxima"
    )
    public ResponseEntity<List<GroupsResponseDTO>> getFullGroups(HttpSession session) {
        logger.debug("Retrieving full groups");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> fullGroups = allSessions.stream()
                .map(this::convertToResponseDTO)
                .filter(GroupsResponseDTO::getIsFull)
                .collect(Collectors.toList());

        logger.info("Found {} full groups", fullGroups.size());
        return ResponseEntity.ok(fullGroups);
    }

    /**
     * Retrieves groups by professor - All authenticated users.
     */
    @GetMapping("/professor/{professorId}")
    @Operation(
            summary = "Grupos por profesor",
            description = "Obtiene todos los grupos asignados a un profesor específico"
    )
    public ResponseEntity<List<GroupsResponseDTO>> getGroupsByProfessor(
            @Parameter(description = "ID del profesor", required = true)
            @PathVariable String professorId,
            HttpSession session) {

        logger.debug("Retrieving groups for professor: {}", professorId);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> professorSessions = classSessionService.searchSessionsByProfessor(professorId);
        List<GroupsResponseDTO> response = professorSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} groups for professor: {}", response.size(), professorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves groups by subject - All authenticated users.
     */
    @GetMapping("/subject/{subjectShortName}")
    @Operation(
            summary = "Grupos por materia",
            description = "Obtiene todos los grupos para una materia específica con información de capacidad"
    )
    public ResponseEntity<List<GroupsResponseDTO>> getGroupsBySubject(
            @Parameter(description = "Nombre corto de la materia", required = true)
            @PathVariable String subjectShortName,
            HttpSession session) {

        logger.debug("Retrieving groups for subject: {}", subjectShortName);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> subjectSessions = classSessionService.searchSessionsBySubjectShortName(subjectShortName);
        List<GroupsResponseDTO> response = subjectSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} groups for subject: {}", response.size(), subjectShortName);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates comprehensive capacity report - All authenticated users.
     */
    @GetMapping("/reports/capacity-summary")
    @Operation(
            summary = "Reporte de resumen de capacidad",
            description = "Genera un reporte completo de capacidad para todos los grupos mostrando estadísticas de inscripción"
    )
    public ResponseEntity<Map<String, Object>> getCapacitySummaryReport(HttpSession session) {
        logger.debug("Generating capacity summary report");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> groupsResponse = allSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        int totalGroups = groupsResponse.size();
        int fullGroups = (int) groupsResponse.stream().filter(GroupsResponseDTO::getIsFull).count();
        int nearCapacityGroups = (int) groupsResponse.stream()
                .filter(group -> group.getOccupancyPercentage() >= 90.0).count();
        
        int totalCapacity = groupsResponse.stream()
                .mapToInt(GroupsResponseDTO::getMaxStudents).sum();
        int totalEnrolled = groupsResponse.stream()
                .mapToInt(GroupsResponseDTO::getCurrentStudents).sum();
        
        double averageOccupancy = groupsResponse.stream()
                .mapToDouble(GroupsResponseDTO::getOccupancyPercentage)
                .average().orElse(0.0);

        Map<String, Object> report = Map.of(
            "totalGroups", totalGroups,
            "fullGroups", fullGroups,
            "nearCapacityGroups", nearCapacityGroups,
            "totalCapacity", totalCapacity,
            "totalEnrolled", totalEnrolled,
            "availableSpots", totalCapacity - totalEnrolled,
            "averageOccupancy", Math.round(averageOccupancy * 100.0) / 100.0,
            "reportGeneratedAt", LocalDateTime.now()
        );

        logger.info("Generated capacity summary report");
        return ResponseEntity.ok(report);
    }

    /**
     * Generates report showing group distribution by subject - All authenticated users.
     */
    @GetMapping("/reports/by-subject")
    @Operation(
            summary = "Reporte de grupos por materia",
            description = "Genera reporte mostrando la distribución de grupos y capacidad por materia"
    )
    public ResponseEntity<Map<String, List<GroupsResponseDTO>>> getGroupsBySubjectReport(HttpSession session) {
        logger.debug("Generating groups by subject report");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        Map<String, List<GroupsResponseDTO>> groupsBySubject = allSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.groupingBy(GroupsResponseDTO::getSubjectShortName));

        logger.info("Generated groups by subject report for {} subjects", groupsBySubject.size());
        return ResponseEntity.ok(groupsBySubject);
    }


    private ClassSession createSessionFromRequest(GroupsRequestDTO request) {
        ClassSession sessionEntity = new ClassSession();
        
        if (request.getSubjectId() != null) {
            try {
                Subject subject = subjectService.searchSubjectById(request.getSubjectId());
                if (subject != null) {
                    sessionEntity.setSubjectShortName(subject.getShortName());
                    sessionEntity.setSubjectName(subject.getName());
                }
            } catch (Exception e) {
                logger.debug("Could not load subject: {}", request.getSubjectId());
            }
        }
        
        sessionEntity.setProfessorId(request.getProfessorId());
        sessionEntity.setCapacity(request.getMaxStudents() != null ? request.getMaxStudents() : 30);
        sessionEntity.setEnrolledStudents(0);
        sessionEntity.setStartDate(LocalDateTime.now());
        sessionEntity.setEndDate(LocalDateTime.now().plusMonths(6)); 
        
        return sessionEntity;
    }

    private void updateSessionFromRequest(ClassSession sessionEntity, GroupsRequestDTO request) {
        if (request.getProfessorId() != null) {
            sessionEntity.setProfessorId(request.getProfessorId());
        }
        if (request.getMaxStudents() != null) {
            sessionEntity.setCapacity(request.getMaxStudents());
        }
        if (request.getSubjectId() != null) {
            try {
                Subject subject = subjectService.searchSubjectById(request.getSubjectId());
                if (subject != null) {
                    sessionEntity.setSubjectShortName(subject.getShortName());
                    sessionEntity.setSubjectName(subject.getName());
                }
            } catch (Exception e) {
                logger.debug("Could not load subject: {}", request.getSubjectId());
            }
        }
    }

    private GroupsResponseDTO convertToResponseDTO(ClassSession sessionEntity) {
        GroupsResponseDTO dto = new GroupsResponseDTO();
        
        dto.setGroupId(sessionEntity.getId());
        dto.setGroupName(sessionEntity.getSubjectShortName() + " - " + sessionEntity.getSubjectName());
        dto.setDescription("Class session for " + sessionEntity.getSubjectName());
        
        dto.setMaxStudents(sessionEntity.getCapacity());
        dto.setCurrentStudents(sessionEntity.getEnrolledStudents());
        dto.setIsFull(sessionEntity.getEnrolledStudents() >= sessionEntity.getCapacity());
        dto.setOccupancyPercentage(
            sessionEntity.getCapacity() > 0 ? 
                ((double) sessionEntity.getEnrolledStudents() / sessionEntity.getCapacity()) * 100.0 : 0.0
        );
        
        if (sessionEntity.getProfessorId() != null) {
            try {
                Professor professor = professorService.searchProfessorById(sessionEntity.getProfessorId());
                if (professor != null) {
                    dto.setProfessorId(professor.getId());
                    dto.setProfessorName(professor.getName());
                    dto.setProfessorEmail(professor.getMail());
                    dto.setProfessorDocument(professor.getDocument());
                }
            } catch (Exception e) {
                logger.debug("Could not load professor: {}", sessionEntity.getProfessorId());
            }
        }
        
        dto.setSubjectShortName(sessionEntity.getSubjectShortName());
        dto.setSubjectName(sessionEntity.getSubjectName());
        dto.setSessionStartDate(sessionEntity.getStartDate());
        dto.setSessionEndDate(sessionEntity.getEndDate());
        dto.setEnrolledStudentIds(sessionEntity.getEnrolledStudentIds());
        dto.setCreationDate(sessionEntity.getStartDate());
        dto.setIsActive(true);
        
        return dto;
    }
}