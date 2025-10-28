package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.controller.dtos.GroupsResponseDTO;
import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.ClassSessionService;
import edu.dosw.sirha.model.services.SubjectService;
import edu.dosw.sirha.model.services.ProfessorService;
import edu.dosw.sirha.model.services.PetitionService;

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
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final PetitionService petitionService;

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

    @PutMapping("/{id}/capacity")
    @Operation(
        summary = "Modificar cupos del grupo",
        description = "Actualiza la capacidad máxima (cupos) de un grupo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capacidad actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Valor de capacidad inválido"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> updateGroupCapacity(
            @PathVariable String id,
            @RequestParam Integer maxStudents,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar cupos");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(id);
        if (sessionEntity == null) {
            return ResponseEntity.notFound().build();
        }

        if (maxStudents == null || maxStudents <= 0) {
            return ResponseEntity.badRequest().build();
        }

        sessionEntity.setCapacity(maxStudents);
        ClassSession updatedSession = classSessionService.updateClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{groupId}/professor/remove")
    @Operation(
        summary = "Retirar profesor del grupo",
        description = "Elimina la asignación de un profesor específico en un grupo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profesor retirado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El profesor no está asignado a este grupo"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<?> removeProfessorFromGroup(
            @PathVariable String groupId,
            @RequestParam String professorCode,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No tienes permisos para retirar profesores"));
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(groupId);
        if (sessionEntity == null) {
            // ¡RETONA DE INMEDIATO!
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "El grupo con ID " + groupId + " no existe"));
        }

        if (!professorCode.equals(sessionEntity.getProfessorCode())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El código de profesor no corresponde al asignado al grupo"));
        }

        sessionEntity.setProfessorCode(null);
        sessionEntity.setProfessorName(null);
        sessionEntity.setProfessorEmail(null);
        sessionEntity.setProfessorDocument(null);

        ClassSession updatedSession = classSessionService.updateClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{groupId}/schedule")
    @Operation(
        summary = "Agregar horario individual a grupo",
        description = "Agrega un horario específico (día, hora, salón) a un grupo"
    )
    public ResponseEntity<GroupsResponseDTO> addScheduleToGroup(
            @PathVariable String groupId,
            @RequestBody GroupsRequestDTO.ScheduleRequest scheduleRequest,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar horarios");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(groupId);
        ClassSchedule newSchedule = new ClassSchedule(
            null,
            scheduleRequest.getDayOfWeek(),
            LocalTime.parse(scheduleRequest.getStartTime()),
            LocalTime.parse(scheduleRequest.getEndTime()),
            scheduleRequest.getClassroom()
        );
        classSessionService.assignScheduleToSession(groupId, newSchedule);

        GroupsResponseDTO response = convertToResponseDTO(classSessionService.searchSessionById(groupId));
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{groupId}/schedule/global")
    @Operation(
        summary = "Agregar horario global a grupo",
        description = "Agrega el mismo horario para varios días a un grupo"
    )
    public ResponseEntity<GroupsResponseDTO> addGlobalScheduleToGroup(
            @PathVariable String groupId,
            @RequestBody GroupsRequestDTO.GlobalScheduleRequest globalRequest,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar horarios");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(groupId);

        for (String day : globalRequest.getDays()) {
            ClassSchedule newSchedule = new ClassSchedule(
                null,
                day,
                LocalTime.parse(globalRequest.getStartTime()),
                LocalTime.parse(globalRequest.getEndTime()),
                globalRequest.getClassroom()
            );
            classSessionService.assignScheduleToSession(groupId, newSchedule);
        }

        GroupsResponseDTO response = convertToResponseDTO(classSessionService.searchSessionById(groupId));
        return ResponseEntity.ok(response);
    }



    @PutMapping("/{groupId}/schedules")
    @Operation(
        summary = "Modificar/agregar varios horarios al grupo",
        description = "Reemplaza o agrega una lista de horarios al grupo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horarios modificados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de horarios inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<GroupsResponseDTO> updateGroupSchedules(
            @PathVariable String groupId,
            @RequestBody List<GroupsRequestDTO.ScheduleRequest> schedules,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar horarios");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(groupId);
        if (sessionEntity == null) {
            return ResponseEntity.notFound().build();
        }

        List<ClassSchedule> newSchedules = schedules.stream()
            .map(s -> new ClassSchedule(
                null,
                s.getDayOfWeek(),
                LocalTime.parse(s.getStartTime()),
                LocalTime.parse(s.getEndTime()),
                s.getClassroom()
            ))
            .toList();

        sessionEntity.setSchedules(newSchedules);
        ClassSession updatedSession = classSessionService.updateClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{groupId}/schedule")
    @Operation(
        summary = "Actualizar horario individual de grupo",
        description = "Actualiza el horario de un día específico en el grupo"
    )
    public ResponseEntity<GroupsResponseDTO> updateIndividualSchedule(
            @PathVariable String groupId,
            @RequestBody GroupsRequestDTO.ScheduleRequest scheduleRequest,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar horarios");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(groupId);
        if (sessionEntity == null) {
            return ResponseEntity.notFound().build();
        }

        boolean updated = false;
        if (sessionEntity.getSchedules() != null) {
            for (ClassSchedule schedule : sessionEntity.getSchedules()) {
                if (schedule.getDayOfWeek().equalsIgnoreCase(scheduleRequest.getDayOfWeek())) {
                    schedule.setStartTime(LocalTime.parse(scheduleRequest.getStartTime()));
                    schedule.setEndTime(LocalTime.parse(scheduleRequest.getEndTime()));
                    schedule.setClassroom(scheduleRequest.getClassroom());
                    updated = true;
                    break;
                }
            }
        }

        if (!updated) {
            return ResponseEntity.badRequest().body(null); 
        }

        ClassSession updatedSession = classSessionService.updateClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{groupId}/schedule/global")
    @Operation(
        summary = "Actualizar horario global de grupo",
        description = "Actualiza el mismo horario para varios días en el grupo"
    )
    public ResponseEntity<GroupsResponseDTO> updateGlobalSchedule(
            @PathVariable String groupId,
            @RequestBody GroupsRequestDTO.GlobalScheduleRequest globalRequest,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar horarios");
        }

        ClassSession sessionEntity = classSessionService.searchSessionById(groupId);
        if (sessionEntity == null) {
            return ResponseEntity.notFound().build();
        }

        int updatedCount = 0;
        if (sessionEntity.getSchedules() != null) {
            for (String day : globalRequest.getDays()) {
                for (ClassSchedule schedule : sessionEntity.getSchedules()) {
                    if (schedule.getDayOfWeek().equalsIgnoreCase(day)) {
                        schedule.setStartTime(LocalTime.parse(globalRequest.getStartTime()));
                        schedule.setEndTime(LocalTime.parse(globalRequest.getEndTime()));
                        schedule.setClassroom(globalRequest.getClassroom());
                        updatedCount++;
                    }
                }
            }
        }

        if (updatedCount == 0) {
            return ResponseEntity.badRequest().body(null); 
        }

        ClassSession updatedSession = classSessionService.updateClassSession(sessionEntity);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-schedule")
    @Operation(
        summary = "Buscar grupos por horario",
        description = "Devuelve los grupos que tienen clase en el día y hora especificados"
    )
    public ResponseEntity<List<Map<String, Object>>> getGroupsBySchedule(
            @RequestParam String dayOfWeek,
            @RequestParam String startTime) {

        List<ClassSession> allSessions = classSessionService.searchAllSessions();

        List<Map<String, Object>> result = allSessions.stream()
            .filter(session -> session.getSchedules() != null)
            .flatMap(session -> session.getSchedules().stream()
                .filter(schedule ->
                    schedule.getDayOfWeek().equalsIgnoreCase(dayOfWeek) &&
                    schedule.getStartTime().toString().equals(startTime)
                )
                .map(schedule -> Map.<String, Object>of(
                    "groupId", session.getId(),
                    "groupName", session.getGroupName(),
                    "classroom", schedule.getClassroom(),
                    "dayOfWeek", schedule.getDayOfWeek(),
                    "startTime", schedule.getStartTime().toString(),
                    "endTime", schedule.getEndTime().toString()
                ))
            )
            .toList();

        return ResponseEntity.ok(result);
    }


    @GetMapping("/search/by-classroom")
    @Operation(
        summary = "Buscar grupos por salón",
        description = "Devuelve los grupos que tienen clase en el salón especificado"
    )
    public ResponseEntity<List<Map<String, Object>>> getGroupsByClassroom(
            @RequestParam String classroom) {

        List<ClassSession> allSessions = classSessionService.searchAllSessions();

        List<Map<String, Object>> result = allSessions.stream()
            .filter(session -> session.getSchedules() != null)
            .flatMap(session -> session.getSchedules().stream()
                .filter(schedule -> schedule.getClassroom().equalsIgnoreCase(classroom))
                .map(schedule -> Map.<String, Object>of(
                    "groupId", session.getId(),
                    "groupName", session.getGroupName(),
                    "classroom", schedule.getClassroom(),
                    "dayOfWeek", schedule.getDayOfWeek(),
                    "startTime", schedule.getStartTime().toString(),
                    "endTime", schedule.getEndTime().toString()
                ))
            )
            .toList();

        return ResponseEntity.ok(result);
    }



    //------------------------------------------------Subjects---------------------------------------------------------------

    private GroupsResponseDTO.SubjectResponseDTO buildSubjectResponse(Subject subject) {
        GroupsResponseDTO.SubjectResponseDTO response = new GroupsResponseDTO.SubjectResponseDTO();
        response.setSubjectId(subject.getId());
        response.setShortName(subject.getShortName());
        response.setName(subject.getName());
        response.setCredits(subject.getCredits());
        response.setLevel(subject.getLevel());
        if (subject.getPrerequisites() != null) {
            response.setPrerequisiteIds(
                subject.getPrerequisites().stream()
                    .map(Subject::getId)
                    .toList()
            );
        }
        List<ClassSession> sessions = classSessionService.searchSessionsBySubjectShortName(subject.getShortName());
        response.setClassSessionIds(sessions.stream().map(ClassSession::getId).toList());
        return response;
    }

    /**
     * Retrieves all subjects in the system.
     * Accessible by all authenticated users.
     *
     * @param session HTTP session for authentication validation.
     * @return List of SubjectResponseDTO containing all subjects.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    @GetMapping("/subjects")
    @Operation(summary = "Obtener todas las materias", description = "Devuelve la lista de todas las materias registradas")
    public ResponseEntity<List<GroupsResponseDTO.SubjectResponseDTO>> getAllSubjects(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        List<Subject> subjects = subjectService.searchAllSubjects();
        List<GroupsResponseDTO.SubjectResponseDTO> response = subjects.stream()
            .map(this::buildSubjectResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific subject by its ID.
     * Accessible by all authenticated users.
     *
     * @param subjectId The ID of the subject to retrieve.
     * @param session   HTTP session for authentication validation.
     * @return SubjectResponseDTO containing the subject details if found, or 404 if not found.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    @GetMapping("/subjects/{subjectId}")
    @Operation(summary = "Obtener materia por ID", description = "Devuelve la información de una materia específica")
    public ResponseEntity<GroupsResponseDTO.SubjectResponseDTO> getSubjectById(
            @PathVariable String subjectId,
            HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        Subject subject = subjectService.searchSubjectById(subjectId);
        if (subject == null) {
            return ResponseEntity.notFound().build();
        }
        GroupsResponseDTO.SubjectResponseDTO response = buildSubjectResponse(subject);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing subject in the system.
     * Only users with DEAN or ACADEMIC_VICEPRESIDENT roles can access this endpoint.
     *
     * @param subjectId The ID of the subject to update.
     * @param request   DTO containing the updated subject data.
     * @param session   HTTP session for authentication and role validation.
     * @return The updated SubjectResponseDTO if successful, or 404 if the subject does not exist.
     * @throws IllegalArgumentException if the user does not have permissions.
     */
    @PutMapping("/subjects/{subjectId}")
    @Operation(summary = "Actualizar materia", description = "Actualiza la información de una materia existente")
    public ResponseEntity<GroupsResponseDTO.SubjectResponseDTO> updateSubject(
            @PathVariable String subjectId,
            @Valid @RequestBody GroupsRequestDTO.SubjectRequest request,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para actualizar materias");
        }

        Subject subject = subjectService.searchSubjectById(subjectId);
        if (subject == null) {
            return ResponseEntity.notFound().build();
        }

        subject.setShortName(request.getSubjectShortName());
        subject.setName(request.getSubjectName());
        subject.setCredits(request.getSubjectCredits());
        subject.setLevel(request.getSubjectLevel());

        if (request.getPrerequisiteIds() != null && !request.getPrerequisiteIds().isEmpty()) {
            List<Subject> prerequisites = request.getPrerequisiteIds().stream()
                .map(id -> subjectService.searchSubjectById(id))
                .filter(s -> s != null)
                .toList();
            subject.setPrerequisites(prerequisites);
        } else {
            subject.setPrerequisites(null);
        }

        

        Subject updated = subjectService.save(subject);
        GroupsResponseDTO.SubjectResponseDTO response = buildSubjectResponse(updated);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new subject in the system.
     * Only users with DEAN or ACADEMIC_VICEPRESIDENT roles can access this endpoint.
     *
     * @param request DTO containing the subject data to be registered.
     * @param session HTTP session for authentication and role validation.
     * @return The created Subject entity with status 201 if successful.
     * @throws IllegalArgumentException if the user does not have permissions or if required data is missing.
     */
    @PostMapping("/subjects")
    @Operation(
        summary = "Registrar materia",
        description = "Crea una nueva materia en el sistema. Solo decanos y vicepresidente académico pueden acceder."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Materia creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de materia inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
        @ApiResponse(responseCode = "409", description = "Materia duplicada")
    })
    public ResponseEntity<GroupsResponseDTO.SubjectResponseDTO> createSubject(
        @Valid @RequestBody GroupsRequestDTO.SubjectRequest request,
            HttpSession session) {

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para registrar materias");
        }

        
        if (request.getSubjectId() == null || request.getSubjectShortName() == null ||
            request.getSubjectName() == null || request.getSubjectCredits() == null ||
            request.getSubjectLevel() == null) {
            throw new IllegalArgumentException("Faltan datos obligatorios para la materia");
        }

        
        Subject subject = new Subject();
        subject.setId(request.getSubjectId());
        subject.setShortName(request.getSubjectShortName());
        subject.setName(request.getSubjectName());
        subject.setCredits(request.getSubjectCredits());
        subject.setLevel(request.getSubjectLevel());

        if (request.getPrerequisiteIds() != null && !request.getPrerequisiteIds().isEmpty()) {
        List<Subject> prerequisites = request.getPrerequisiteIds().stream()
            .map(id -> subjectService.searchSubjectById(id))
            .filter(s -> s != null)
            .toList();
        subject.setPrerequisites(prerequisites);
    }

        
        Subject saved = subjectService.createSubject(subject);
        GroupsResponseDTO.SubjectResponseDTO response = buildSubjectResponse(saved);

        return ResponseEntity.status(201).body(response);
    }

    /**
     * Deletes a subject from the system.
     * Only users with DEAN or ACADEMIC_VICEPRESIDENT roles can access this endpoint.
     *
     * @param subjectId The ID of the subject to delete.
     * @param session   HTTP session for authentication and role validation.
     * @return 204 No Content if deletion is successful, or 404 if the subject does not exist.
     * @throws IllegalArgumentException if the user does not have permissions.
     */
    @DeleteMapping("/subjects/{subjectId}")
    @Operation(summary = "Eliminar materia", description = "Elimina una materia del sistema")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable String subjectId,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para eliminar materias");
        }

        Subject subject = subjectService.searchSubjectById(subjectId);
        if (subject == null) {
            return ResponseEntity.notFound().build();
        }

        subjectService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }

//-------------------------------------------------- Professors --------------------------------------------------


    private GroupsResponseDTO.ProfessorResponseDTO buildProfessorResponse(Professor professor) {
        GroupsResponseDTO.ProfessorResponseDTO response = new GroupsResponseDTO.ProfessorResponseDTO();
        response.setProfessorCode(professor.getProfessorCode());
        response.setName(professor.getName());
        response.setMail(professor.getMail());
        response.setDocument(professor.getDocument());

        if (professor.getProfessorCode() != null && !professor.getProfessorCode().isEmpty()) {
            List<ClassSession> sessions = classSessionService.searchSessionsByProfessor(professor.getProfessorCode());
            response.setGroupIds(sessions.stream().map(ClassSession::getId).toList());
        } else {
            response.setGroupIds(List.of());
        }

        return response;
    }
    /**
     * Registers a new professor in the system.
     * Only users with DEAN or ACADEMIC_VICEPRESIDENT roles can access this endpoint.
     *
     * @param request DTO containing the professor data to be registered.
     * @param session HTTP session for authentication and role validation.
     * @return The created ProfessorResponseDTO with status 201 if successful.
     * @throws IllegalArgumentException if the user does not have permissions or if required data is missing.
     */
    @PostMapping("/professors")
    @Operation(
        summary = "Registrar profesor",
        description = "Crea un nuevo profesor en el sistema. Solo decanos y vicepresidente académico pueden acceder."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Profesor creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de profesor inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes - Solo decanos y vicepresidente académico"),
        @ApiResponse(responseCode = "409", description = "Profesor duplicado")
    })
    public ResponseEntity<GroupsResponseDTO.ProfessorResponseDTO> registerProfessor(
            @Valid @RequestBody GroupsRequestDTO.ProfessorRequest request,
            HttpSession session) {

        logger.info("Registering new professor: {}", request.getDocument());

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para registrar profesores");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        
        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());
        userDTO.setType(UserType.PROFESSOR);

        Professor professor = professorService.createProfessor(userDTO);

        
        professor.setProfessorCode(request.getProfessorCode());
        professorService.save(professor); 

        GroupsResponseDTO.ProfessorResponseDTO response = buildProfessorResponse(professor);

        logger.info("Professor created successfully with ID: {} by user: {}", professor.getId(), currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all professors in the system - All authenticated users.
     *
     * @param session HTTP session for authentication.
     * @return List of ProfessorResponseDTO with status 200 if successful.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    @GetMapping("/professors")
    @Operation(summary = "Obtener todos los profesores", description = "Devuelve la lista de todos los profesores registrados")
    public ResponseEntity<List<GroupsResponseDTO.ProfessorResponseDTO>> getAllProfessors(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        List<Professor> professors = professorService.searchAllProfessors();
        List<GroupsResponseDTO.ProfessorResponseDTO> response = professors.stream()
        .filter(prof -> prof.getProfessorCode() != null && !prof.getProfessorCode().isEmpty())
        .map(this::buildProfessorResponse)
        .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific professor by code - All authenticated users.
     *
     * @param professorCode      The code of the professor to retrieve.
     * @param session HTTP session for authentication.
     * @return ProfessorResponseDTO with status 200 if found, 404 if not found.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    @GetMapping("/professors/code/{professorCode}")
    @Operation(summary = "Obtener profesor por código", description = "Devuelve la información de un profesor por su código")
    public ResponseEntity<GroupsResponseDTO.ProfessorResponseDTO> getProfessorByCode(
            @PathVariable String professorCode,
            HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        Professor professor = professorService.searchProfessorByCode(professorCode);
        if (professor == null) {
            return ResponseEntity.notFound().build();
        }
        GroupsResponseDTO.ProfessorResponseDTO response = buildProfessorResponse(professor);
        return ResponseEntity.ok(response);
}

    /**
     * Updates a professor's information by code - DEAN, ACADEMIC_VICEPRESIDENT, and PROFESSOR (self-update) only.
     *
     * @param professorCode The code of the professor to update.
     * @param request       DTO containing the updated professor data.
     * @param session HTTP session for authentication and role validation.
     * @return Updated ProfessorResponseDTO with status 200 if successful, 404 if not found.
     * @throws IllegalArgumentException if the user does not have permissions.
     */
    @PutMapping("/professors/code/{professorCode}")
    @Operation(summary = "Actualizar profesor por código", description = "Actualiza la información de un profesor usando su código")
    public ResponseEntity<GroupsResponseDTO.ProfessorResponseDTO> updateProfessorByCode(
            @PathVariable String professorCode,
            @Valid @RequestBody GroupsRequestDTO.ProfessorRequest request,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT, UserType.PROFESSOR);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para actualizar profesores");
        }

        Professor professor = professorService.searchProfessorByCode(professorCode);
        if (professor == null) {
            return ResponseEntity.notFound().build();
        }

        professor.setName(request.getName());
        professor.setMail(request.getMail());
        professor.setDocument(request.getDocument());
        professor.setProfessorCode(request.getProfessorCode());

        Professor updated = professorService.save(professor);
        GroupsResponseDTO.ProfessorResponseDTO response = buildProfessorResponse(updated);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a professor by ID - DEAN and ACADEMIC_VICEPRESIDENT only.
     *
     * @param professorCode      The ID of the professor to delete.
     * @param session HTTP session for authentication and role validation.
     * @return ResponseEntity with status 204 if deleted, 404 if not found.
     * @throws IllegalArgumentException if the user does not have permissions.
     */
    @DeleteMapping("/professors/code/{professorCode}")
    @Operation(summary = "Eliminar profesor", description = "Elimina un profesor del sistema")
    public ResponseEntity<Void> deleteProfessor(
            @PathVariable String professorCode,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
            session, UserType.DEAN, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para eliminar profesores");
        }

        Professor professor = professorService.searchProfessorByCode(professorCode);
        if (professor == null) {
            return ResponseEntity.notFound().build();
        }

        professorService.deleteProfessorByCode(professorCode);
        return ResponseEntity.noContent().build();
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
        sessionEntity.setProfessorCode(professorId);
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
        if (sessionEntity == null) {
            throw new IllegalArgumentException("Session not found"); // Tu handler debe mapear esto a 404
        }
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

       
        if (request.getGroupId() != null) {
            sessionEntity.setId(request.getGroupId());
        }

        
        if (request.getGroupName() != null) {
            sessionEntity.setGroupName(request.getGroupName());
        }

        
        if (request.getDescription() != null) {
            sessionEntity.setDescription(request.getDescription());
        }

       
        if (request.getSubjectId() != null) {
        Subject subject = subjectService.searchSubjectById(request.getSubjectId());
        if (subject == null) {
            throw new IllegalArgumentException("La materia con ID " + request.getSubjectId() + " no existe");
        }
        sessionEntity.setSubjectId(request.getSubjectId());
        sessionEntity.setSubjectShortName(subject.getShortName());
        sessionEntity.setSubjectName(subject.getName());
        sessionEntity.setSubjectCredits(subject.getCredits());
        sessionEntity.setSubjectLevel(subject.getLevel());
        }

        if (request.getProfessorCode() != null) {
        Professor professor = professorService.searchProfessorByCode(request.getProfessorCode());
        if (professor == null) {
            throw new IllegalArgumentException("El profesor con código " + request.getProfessorCode() + " no existe");
        }

        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            List<ClassSchedule> schedules = request.getSchedules().stream()
                .map(s -> new ClassSchedule(
                    null, 
                    s.getDayOfWeek(),
                    LocalTime.parse(s.getStartTime()),
                    LocalTime.parse(s.getEndTime()),
                    s.getClassroom()
                ))
                .toList();
            sessionEntity.setSchedules(schedules);
        }

        sessionEntity.setProfessorCode(request.getProfessorCode());
        sessionEntity.setProfessorName(professor.getName());
        sessionEntity.setProfessorEmail(professor.getMail());
        sessionEntity.setProfessorDocument(professor.getDocument());
        }
        
        sessionEntity.setCapacity(request.getMaxStudents() != null ? request.getMaxStudents() : 30);

        sessionEntity.setEnrolledStudents(0);
        sessionEntity.setStartDate(LocalDateTime.now());
        sessionEntity.setEndDate(LocalDateTime.now().plusMonths(6)); 

        return sessionEntity;
    }
    private void updateSessionFromRequest(ClassSession sessionEntity, GroupsRequestDTO request) {
        if (request.getGroupName() != null) {
            sessionEntity.setGroupName(request.getGroupName());
        }
        if (request.getDescription() != null) {
            sessionEntity.setDescription(request.getDescription());
        }
        if (request.getProfessorCode() != null) {
            Professor professor = professorService.searchProfessorByCode(request.getProfessorCode());
            if (professor == null) {
                throw new IllegalArgumentException("El profesor con código " + request.getProfessorCode() + " no existe");
            }
            sessionEntity.setProfessorCode(professor.getProfessorCode());
            sessionEntity.setProfessorName(professor.getName());
            sessionEntity.setProfessorEmail(professor.getMail());
            sessionEntity.setProfessorDocument(professor.getDocument());
        }
        if (request.getSubjectId() != null) {
            Subject subject = subjectService.searchSubjectById(request.getSubjectId());
            if (subject == null) {
                throw new IllegalArgumentException("La materia con ID " + request.getSubjectId() + " no existe");
            }
            sessionEntity.setSubjectId(subject.getId());
            sessionEntity.setSubjectShortName(subject.getShortName());
            sessionEntity.setSubjectName(subject.getName());
            sessionEntity.setSubjectCredits(subject.getCredits());
            sessionEntity.setSubjectLevel(subject.getLevel());
        }
        if (request.getMaxStudents() != null) {
            sessionEntity.setCapacity(request.getMaxStudents());
        }
    }

    private GroupsResponseDTO convertToResponseDTO(ClassSession sessionEntity) {
        GroupsResponseDTO dto = new GroupsResponseDTO();

        dto.setGroupId(sessionEntity.getId());
        dto.setGroupName(sessionEntity.getGroupName());
        dto.setDescription(sessionEntity.getDescription());

        dto.setMaxStudents(sessionEntity.getCapacity());
        dto.setCurrentStudents(sessionEntity.getEnrolledStudents());
        dto.setIsFull(sessionEntity.getEnrolledStudents() >= sessionEntity.getCapacity());
        dto.setOccupancyPercentage(
            sessionEntity.getCapacity() > 0 ?
                ((double) sessionEntity.getEnrolledStudents() / sessionEntity.getCapacity()) * 100.0 : 0.0
        );

        dto.setProfessorCode(sessionEntity.getProfessorCode());

        
        if (sessionEntity.getProfessorCode() != null) {
            try {
                Professor professor = professorService.searchProfessorByCode(sessionEntity.getProfessorCode());
                if (professor != null) {
                    //dto.setProfessorCode(professor.getProfessorCode());
                    dto.setProfessorName(professor.getName());
                    dto.setProfessorEmail(professor.getMail());
                    dto.setProfessorDocument(professor.getDocument());
                }
            } catch (Exception e) {
                logger.debug("Could not load professor: {}", sessionEntity.getProfessorCode());
            }
        }

        if (sessionEntity.getSchedules() != null) {
            List<Map<String, String>> scheduleInfo = sessionEntity.getSchedules().stream()
                .map(schedule -> Map.of(
                    "dayOfWeek", schedule.getDayOfWeek(),
                    "startTime", schedule.getStartTime().toString(),
                    "endTime", schedule.getEndTime().toString(),
                    "classroom", schedule.getClassroom()
                ))
                .toList();
            dto.setSchedules(scheduleInfo);
        } else {
            dto.setSchedules(List.of());
        }

        dto.setSubjectId(sessionEntity.getSubjectId());
        dto.setSubjectShortName(sessionEntity.getSubjectShortName());
        dto.setSubjectName(sessionEntity.getSubjectName());
        dto.setSubjectCredits(sessionEntity.getSubjectCredits());
        dto.setSubjectLevel(sessionEntity.getSubjectLevel());
        dto.setSessionStartDate(sessionEntity.getStartDate());
        dto.setSessionEndDate(sessionEntity.getEndDate());
        dto.setEnrolledStudentIds(sessionEntity.getEnrolledStudentIds());
        dto.setCreationDate(sessionEntity.getStartDate());
        dto.setIsActive(true);



        return dto;
    }

    /* */ 

    /**
     * Generates basic statistics for the most requested groups for changes - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/most-requested-changes")
    @Operation(summary = "Grupos más solicitados para cambio")
    public ResponseEntity<Map<String, Object>> getMostRequestedGroupChanges(HttpSession session) {
        logger.debug("Generating most requested group changes statistics");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                UserType.DEAN, 
                                                                                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver estadísticas de cambios de grupo");
        }

        List<Petition> changeGroupPetitions = petitionService.searchPetitionByType(PetitionType.CHANGE_GROUP);
        Map<String, Object> statistics = generateBasicGroupChangeStatistics(changeGroupPetitions);

        logger.info("Generated group change statistics with {} petitions", changeGroupPetitions.size());
        return ResponseEntity.ok(statistics);
    }

    /**
     * Generates basic detailed change request statistics - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/change-request-statistics")
    @Operation(summary = "Estadísticas detalladas de solicitudes de cambio")
    public ResponseEntity<Map<String, Object>> getDetailedChangeRequestStatistics(HttpSession session) {
        logger.debug("Generating detailed change request statistics");

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                UserType.DEAN, 
                                                                                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver estadísticas detalladas");
        }

        List<Petition> allChangePetitions = petitionService.searchPetitionByType(PetitionType.CHANGE_GROUP);
        Map<String, Object> detailedStatistics = generateBasicDetailedStatistics(allChangePetitions);

        logger.info("Generated detailed change statistics");
        return ResponseEntity.ok(detailedStatistics);
    }


    /**
     * Generates basic statistics for group change requests.
     */
    private Map<String, Object> generateBasicGroupChangeStatistics(List<Petition> changeGroupPetitions) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalChangeRequests", changeGroupPetitions.size());
        
        Map<String, Long> subjectCounts = changeGroupPetitions.stream()
                .filter(p -> p.getSubjectShortName() != null)
                .collect(Collectors.groupingBy(
                        Petition::getSubjectShortName,
                        Collectors.counting()
                ));
        
        List<Map<String, Object>> topRequestedSubjects = subjectCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> Map.<String, Object>of(
                        "subjectShortName", entry.getKey(),
                        "changeRequestCount", entry.getValue()
                ))
                .collect(Collectors.toList());
        
        stats.put("topRequestedSubjects", topRequestedSubjects);
        
        Map<String, Long> changesByState = changeGroupPetitions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getState().name(),
                        Collectors.counting()
                ));
        stats.put("changeRequestsByState", changesByState);
        
        stats.put("generatedAt", LocalDateTime.now());
        return stats;
    }

    /**
     * Generates basic detailed change statistics.
     */
    private Map<String, Object> generateBasicDetailedStatistics(List<Petition> changePetitions) {
        Map<String, Object> detailedStats = new LinkedHashMap<>();
        if (changePetitions == null) {
            changePetitions = List.of();
        }
        detailedStats.put("totalRequests", changePetitions.size());

        Map<String, Map<String, Long>> subjectStateAnalysis = changePetitions.stream()
                .filter(p -> p.getSubjectShortName() != null)
                .collect(Collectors.groupingBy(
                        Petition::getSubjectShortName,
                        Collectors.groupingBy(
                                p -> p.getState() == null ? "SIN_ESTADO" : p.getState().name(),
                                Collectors.counting()
                        )
                ));
        detailedStats.put("subjectStateAnalysis", subjectStateAnalysis);

        Map<String, Long> studentActivity = changePetitions.stream()
                .collect(Collectors.groupingBy(
                        Petition::getStudentId,
                        Collectors.counting()
                ));

        List<Map<String, Object>> mostActiveStudents = studentActivity.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> Map.<String, Object>of(
                        "studentId", entry.getKey(),
                        "changeRequestCount", entry.getValue()
                ))
                .collect(Collectors.toList());
        detailedStats.put("mostActiveStudents", mostActiveStudents);

        long approvedRequests = changePetitions.stream()
                .filter(p -> p.getState() == PetitionState.APPROVED)
                .count();

        double successRate = changePetitions.isEmpty() ? 0.0 :
                Math.round((double) approvedRequests / changePetitions.size() * 100 * 100.0) / 100.0;
        detailedStats.put("overallSuccessRate", successRate);

        detailedStats.put("generatedAt", LocalDateTime.now());
        return detailedStats;
    }
    
}