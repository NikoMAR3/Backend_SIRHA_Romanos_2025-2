package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.controller.dtos.GroupsResponseDTO;
import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.entities.ClassSchedule;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.services.ClassSessionService;
import edu.dosw.sirha.model.services.SubjectService;
import edu.dosw.sirha.model.services.ProfessorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
     * Creates a new academic group with capacity validation and schedule conflict detection.
     *
     * @param request the group creation request
     * @return the created group information
     * @throws IllegalArgumentException if group data is invalid or conflicts exist
     */
    @Operation(
            summary = "Crear nuevo grupo académico",
            description = "Crea un nuevo grupo académico con validación automática de capacidad y detección de conflictos de horario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de grupo inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto de horarios o capacidad excedida")
    })
    @PostMapping
    public ResponseEntity<GroupsResponseDTO> createGroup(
            @Valid @RequestBody GroupsRequestDTO request) {

        logger.info("Creating new group: {} for subject: {}", 
                   request.getGroupName(), request.getSubjectId());

        ClassSession session = createSessionFromRequest(request);
        ClassSession savedSession = classSessionService.createClassSession(session);
        GroupsResponseDTO response = convertToResponseDTO(savedSession);

        logger.info("Group created successfully with ID: {}", savedSession.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves detailed information about a specific group.
     *
     * @param id the ID of the group to retrieve
     * @return complete group information including enrollment statistics
     */
    @Operation(
            summary = "Obtener información del grupo",
            description = "Obtiene información detallada de un grupo específico incluyendo estadísticas de inscripción"
    )
    @GetMapping("/{id}")
    public ResponseEntity<GroupsResponseDTO> getGroupById(
            @Parameter(description = "Identificador único del grupo", required = true)
            @PathVariable String id) {

        logger.debug("Retrieving group with ID: {}", id);

        ClassSession session = classSessionService.searchSessionById(id);
        GroupsResponseDTO response = convertToResponseDTO(session);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all academic groups with their current status.
     *
     * @return list of all groups with capacity and enrollment information
     */
    @Operation(
            summary = "Listar todos los grupos",
            description = "Obtiene todos los grupos académicos con su estado actual de capacidad e inscripciones"
    )
    @GetMapping
    public ResponseEntity<List<GroupsResponseDTO>> getAllGroups() {
        logger.debug("Retrieving all groups");

        List<ClassSession> sessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> response = sessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} groups", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates group information with validation for capacity limits and schedule conflicts.
     *
     * @param id the ID of the group to update
     * @param request the update request with new information
     * @return updated group information
     */
    @Operation(
            summary = "Actualizar información del grupo",
            description = "Actualiza la información del grupo con validación de límites de capacidad y conflictos de horario"
    )
    @PutMapping("/{id}")
    public ResponseEntity<GroupsResponseDTO> updateGroup(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Valid @RequestBody GroupsRequestDTO request) {

        logger.info("Updating group with ID: {}", id);

        ClassSession existingSession = classSessionService.searchSessionById(id);
        updateSessionFromRequest(existingSession, request);
        ClassSession updatedSession = classSessionService.updateClassSession(existingSession);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        logger.info("Group updated successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a group after validating no active enrollments exist.
     *
     * @param id the ID of the group to delete
     * @return deletion confirmation
     */
    @Operation(
            summary = "Eliminar grupo",
            description = "Elimina un grupo después de validar que no existen inscripciones activas"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID del grupo a eliminar", required = true)
            @PathVariable String id) {

        logger.info("Deleting group with ID: {}", id);

        try {
            classSessionService.deleteClassSession(id);
            logger.info("Group deleted successfully with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.warn("Group not found for deletion with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Retrieves detailed capacity information for a specific group.
     *
     * @param id the ID of the group
     * @return capacity statistics including enrolled students and occupancy percentage
     */
    @Operation(
            summary = "Consultar información de capacidad",
            description = "Obtiene información detallada de capacidad incluyendo estudiantes inscritos y porcentaje de ocupación"
    )
    @GetMapping("/{id}/capacity")
    public ResponseEntity<Map<String, Object>> getGroupCapacity(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id) {

        logger.debug("Retrieving capacity information for group: {}", id);

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
     * Retrieves all groups that are at or above 90% capacity.
     *
     * @return list of groups near capacity for monitoring purposes
     */
    @Operation(
            summary = "Grupos cerca del límite de capacidad",
            description = "Obtiene todos los grupos que están al 90% o más de su capacidad para propósitos de monitoreo"
    )
    @GetMapping("/reports/near-capacity")
    public ResponseEntity<List<GroupsResponseDTO>> getGroupsNearCapacity() {
        logger.debug("Retrieving groups near capacity");

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> nearCapacityGroups = allSessions.stream()
                .map(this::convertToResponseDTO)
                .filter(group -> group.getOccupancyPercentage() >= 90.0)
                .collect(Collectors.toList());

        logger.info("Found {} groups near capacity", nearCapacityGroups.size());
        return ResponseEntity.ok(nearCapacityGroups);
    }

    /**
     * Retrieves all groups that have reached maximum capacity.
     *
     * @return list of groups at full capacity
     */
    @Operation(
            summary = "Grupos con capacidad completa",
            description = "Obtiene todos los grupos que han alcanzado su capacidad máxima"
    )
    @GetMapping("/reports/full")
    public ResponseEntity<List<GroupsResponseDTO>> getFullGroups() {
        logger.debug("Retrieving full groups");

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        List<GroupsResponseDTO> fullGroups = allSessions.stream()
                .map(this::convertToResponseDTO)
                .filter(GroupsResponseDTO::getIsFull)
                .collect(Collectors.toList());

        logger.info("Found {} full groups", fullGroups.size());
        return ResponseEntity.ok(fullGroups);
    }

    /**
     * Assigns a professor to a group with validation for schedule conflicts.
     *
     * @param id the ID of the group
     * @param professorId the ID of the professor to assign
     * @return updated group information
     */
    @Operation(
            summary = "Asignar profesor al grupo",
            description = "Asigna un profesor a un grupo con validación de conflictos de horario"
    )
    @PutMapping("/{id}/professor")
    public ResponseEntity<GroupsResponseDTO> assignProfessor(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Parameter(description = "ID del profesor a asignar", required = true)
            @RequestParam String professorId) {

        logger.info("Assigning professor {} to group {}", professorId, id);

        ClassSession session = classSessionService.searchSessionById(id);
        session.setProfessorId(professorId);
        ClassSession updatedSession = classSessionService.updateClassSession(session);
        GroupsResponseDTO response = convertToResponseDTO(updatedSession);

        logger.info("Professor assigned successfully to group: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all groups assigned to a specific professor.
     *
     * @param professorId the ID of the professor
     * @return list of groups assigned to the professor
     */
    @Operation(
            summary = "Grupos por profesor",
            description = "Obtiene todos los grupos asignados a un profesor específico"
    )
    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<GroupsResponseDTO>> getGroupsByProfessor(
            @Parameter(description = "ID del profesor", required = true)
            @PathVariable String professorId) {

        logger.debug("Retrieving groups for professor: {}", professorId);

        List<ClassSession> professorSessions = classSessionService.searchSessionsByProfessor(professorId);
        List<GroupsResponseDTO> response = professorSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} groups for professor: {}", response.size(), professorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all groups for a specific subject.
     *
     * @param subjectShortName the short name of the subject
     * @return list of groups for the specified subject
     */
    @Operation(
            summary = "Grupos por materia",
            description = "Obtiene todos los grupos para una materia específica con información de capacidad"
    )
    @GetMapping("/subject/{subjectShortName}")
    public ResponseEntity<List<GroupsResponseDTO>> getGroupsBySubject(
            @Parameter(description = "Nombre corto de la materia", required = true)
            @PathVariable String subjectShortName) {

        logger.debug("Retrieving groups for subject: {}", subjectShortName);

        List<ClassSession> subjectSessions = classSessionService.searchSessionsBySubjectShortName(subjectShortName);
        List<GroupsResponseDTO> response = subjectSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} groups for subject: {}", response.size(), subjectShortName);
        return ResponseEntity.ok(response);
    }

    /**
     * Enrolls a student in a group with capacity and schedule conflict validation.
     *
     * @param id the ID of the group
     * @param studentId the ID of the student to enroll
     * @return updated group information
     */
    @Operation(
            summary = "Inscribir estudiante en grupo",
            description = "Inscribe un estudiante en un grupo con validación de capacidad y conflictos de horario"
    )
    @PostMapping("/{id}/students/{studentId}")
    public ResponseEntity<GroupsResponseDTO> enrollStudent(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId) {

        logger.info("Enrolling student {} in group {}", studentId, id);

        ClassSession session = classSessionService.enrollStudent(id, studentId);
        GroupsResponseDTO response = convertToResponseDTO(session);

        logger.info("Student enrolled successfully in group: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraws a student from a group and automatically enrolls next student from waiting list.
     *
     * @param id the ID of the group
     * @param studentId the ID of the student to withdraw
     * @return updated group information
     */
    @Operation(
            summary = "Retirar estudiante del grupo",
            description = "Retira un estudiante del grupo e inscribe automáticamente al siguiente estudiante de la lista de espera"
    )
    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<GroupsResponseDTO> withdrawStudent(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id,
            @Parameter(description = "ID del estudiante", required = true)
            @PathVariable String studentId) {

        logger.info("Withdrawing student {} from group {}", studentId, id);

        ClassSession session = classSessionService.withdrawStudent(id, studentId);
        GroupsResponseDTO response = convertToResponseDTO(session);

        logger.info("Student withdrawn successfully from group: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves list of students enrolled in a specific group.
     *
     * @param id the ID of the group
     * @return list of enrolled student IDs
     */
    @Operation(
            summary = "Estudiantes inscritos",
            description = "Obtiene la lista de estudiantes inscritos en un grupo específico"
    )
    @GetMapping("/{id}/students")
    public ResponseEntity<List<String>> getEnrolledStudents(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id) {

        logger.debug("Retrieving enrolled students for group: {}", id);

        ClassSession session = classSessionService.searchSessionById(id);
        List<String> enrolledStudents = session.getEnrolledStudentIds();
        
        logger.info("Retrieved {} enrolled students for group: {}", 
                   enrolledStudents != null ? enrolledStudents.size() : 0, id);
        return ResponseEntity.ok(enrolledStudents != null ? enrolledStudents : List.of());
    }

    /**
     * Gets students on waiting list for a group.
     *
     * @param id the ID of the group
     * @return list of student IDs on waiting list
     */
    @Operation(
            summary = "Lista de espera",
            description = "Obtiene la lista de estudiantes en lista de espera para un grupo específico"
    )
    @GetMapping("/{id}/waiting-list")
    public ResponseEntity<List<String>> getWaitingList(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id) {

        logger.debug("Retrieving waiting list for group: {}", id);

        ClassSession session = classSessionService.searchSessionById(id);
        List<String> waitingList = session.getWaitingListStudentIds();
        
        return ResponseEntity.ok(waitingList != null ? waitingList : List.of());
    }
    /**
     * Retrieves all schedule information for a specific group.
     *
     * @param id the ID of the group
     * @return list of schedules associated with the group
     */
    @Operation(
            summary = "Horarios del grupo",
            description = "Obtiene toda la información de horarios para un grupo específico"
    )
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<Map<String, String>>> getGroupSchedules(
            @Parameter(description = "ID del grupo", required = true)
            @PathVariable String id) {

        logger.debug("Retrieving schedules for group: {}", id);

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
     * Generates comprehensive capacity report for all groups.
     *
     * @return capacity summary report with enrollment statistics
     */
    @Operation(
            summary = "Reporte de resumen de capacidad",
            description = "Genera un reporte completo de capacidad para todos los grupos mostrando estadísticas de inscripción"
    )
    @GetMapping("/reports/capacity-summary")
    public ResponseEntity<Map<String, Object>> getCapacitySummaryReport() {
        logger.debug("Generating capacity summary report");

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
     * Generates report showing group distribution by subject.
     *
     * @return subject distribution report
     */
    @Operation(
            summary = "Reporte de grupos por materia",
            description = "Genera reporte mostrando la distribución de grupos y capacidad por materia"
    )
    @GetMapping("/reports/by-subject")
    public ResponseEntity<Map<String, List<GroupsResponseDTO>>> getGroupsBySubjectReport() {
        logger.debug("Generating groups by subject report");

        List<ClassSession> allSessions = classSessionService.searchAllSessions();
        Map<String, List<GroupsResponseDTO>> groupsBySubject = allSessions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.groupingBy(GroupsResponseDTO::getSubjectShortName));

        logger.info("Generated groups by subject report for {} subjects", groupsBySubject.size());
        return ResponseEntity.ok(groupsBySubject);
    }

    /**
     * Creates a ClassSession entity from the request DTO.
     */
    private ClassSession createSessionFromRequest(GroupsRequestDTO request) {
        ClassSession session = new ClassSession();
        
        if (request.getSubjectId() != null) {
            try {
                Subject subject = subjectService.searchSubjectById(request.getSubjectId());
                if (subject != null) {
                    session.setSubjectShortName(subject.getShortName());
                    session.setSubjectName(subject.getName());
                }
            } catch (Exception e) {
                logger.debug("Could not load subject: {}", request.getSubjectId());
            }
        }
        
        session.setProfessorId(request.getProfessorId());
        session.setCapacity(request.getMaxStudents() != null ? request.getMaxStudents() : 30);
        session.setEnrolledStudents(0);
        session.setStartDate(LocalDateTime.now());
        session.setEndDate(LocalDateTime.now().plusMonths(6)); 
        
        return session;
    }

    /**
     * Updates an existing ClassSession entity from the request DTO.
     */
    private void updateSessionFromRequest(ClassSession session, GroupsRequestDTO request) {
        if (request.getProfessorId() != null) {
            session.setProfessorId(request.getProfessorId());
        }
        if (request.getMaxStudents() != null) {
            session.setCapacity(request.getMaxStudents());
        }
        if (request.getSubjectId() != null) {
            try {
                Subject subject = subjectService.searchSubjectById(request.getSubjectId());
                if (subject != null) {
                    session.setSubjectShortName(subject.getShortName());
                    session.setSubjectName(subject.getName());
                }
            } catch (Exception e) {
                logger.debug("Could not load subject: {}", request.getSubjectId());
            }
        }
    }

    /**
     * Converts a ClassSession entity to GroupsResponseDTO.
     */
    private GroupsResponseDTO convertToResponseDTO(ClassSession session) {
        GroupsResponseDTO dto = new GroupsResponseDTO();
        

        dto.setGroupId(session.getId());
        dto.setGroupName(session.getSubjectShortName() + " - " + session.getSubjectName());
        dto.setDescription("Class session for " + session.getSubjectName());
        

        dto.setMaxStudents(session.getCapacity());
        dto.setCurrentStudents(session.getEnrolledStudents());
        dto.setIsFull(session.getEnrolledStudents() >= session.getCapacity());
        dto.setOccupancyPercentage(
            session.getCapacity() > 0 ? 
                ((double) session.getEnrolledStudents() / session.getCapacity()) * 100.0 : 0.0
        );
        
        if (session.getProfessorId() != null) {
            try {
                Professor professor = professorService.searchProfessorById(session.getProfessorId());
                if (professor != null) {
                    dto.setProfessorId(professor.getId());
                    dto.setProfessorName(professor.getName());
                    dto.setProfessorEmail(professor.getMail());
                    dto.setProfessorDocument(professor.getDocument());
                }
            } catch (Exception e) {
                logger.debug("Could not load professor: {}", session.getProfessorId());
            }
        }
        
        dto.setSubjectShortName(session.getSubjectShortName());
        dto.setSubjectName(session.getSubjectName());
        

        dto.setSessionStartDate(session.getStartDate());
        dto.setSessionEndDate(session.getEndDate());
        
        dto.setEnrolledStudentIds(session.getEnrolledStudentIds());
        dto.setCreationDate(session.getStartDate());
        dto.setIsActive(true);
        
        return dto;
    }
}