package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.controller.dtos.PetitionResponseDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.PetitionService;
import edu.dosw.sirha.model.services.DeanService;
import edu.dosw.sirha.model.components.util.PetitionCreator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/petitions")
@Tag(name = "Petition Management", description = "Endpoints for comprehensive management of academic petitions with automatic routing and traceability")
public class PetitionsController {

    private static final Logger logger = LoggerFactory.getLogger(PetitionsController.class);

    private final PetitionService petitionService;
    private final List<PetitionCreator> petitionCreators;
        private final DeanService deanService;

    @Autowired
    public PetitionsController(PetitionService petitionService, List<PetitionCreator> petitionCreators, DeanService deanService) {
        this.petitionService = petitionService;
        this.petitionCreators = petitionCreators;
        this.deanService = deanService;
    }

    /**
     * Creates a new petition - STUDENT only (for their own petitions).
     */
    @PostMapping
    @Operation(
            summary = "Create new petition",
            description = "Receives and creates a new petition with automatic routing by faculty and automatic priority assignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Petition created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid petition data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only students can create petitions for themselves"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PetitionResponseDTO> createPetition(
            @Valid @RequestBody PetitionRequestDTO petitionRequest,
            HttpSession session) {

        logger.info("Creating new petition for student: {}", petitionRequest.getUserID());

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.STUDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo estudiantes pueden crear solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

       
        if (!currentUser.getId().equals(petitionRequest.getUserID())) {
            logger.warn("Student {} attempted to create petition for student {}", 
                       currentUser.getId(), petitionRequest.getUserID());
            throw new IllegalArgumentException("Solo puedes crear solicitudes para ti mismo");
        }

        PetitionCreator selectedCreator = selectPetitionCreator(petitionRequest);
        Petition petition = selectedCreator.createPetition(petitionRequest);
        Petition savedPetition = petitionService.createPetition(petition);
        PetitionResponseDTO response = convertToResponseDTO(savedPetition);

        logger.info("Petition created successfully with ID: {} by student: {}", 
                   savedPetition.getPetitionId(), currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific petition - Students can only see their own, Deans/VP can see all.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get petition by ID",
            description = "Retrieves a specific petition with all its information and traceability"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Petition found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to view this petition"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")
    })
    public ResponseEntity<PetitionResponseDTO> getPetitionById(
            @Parameter(description = "Unique petition identifier", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.debug("Retrieving petition with ID: {}", id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        Petition petition = petitionService.searchPetitionsById(id);

        
        validatePetitionAccess(currentUser, petition);

        PetitionResponseDTO response = convertToResponseDTO(petition);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all petitions - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping
    @Operation(
            summary = "List all petitions",
            description = "Retrieves all petitions in the system ordered by creation date"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of petitions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getAllPetitions(HttpSession session) {
        logger.debug("Retrieving all petitions");

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver todas las solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> petitions = getPetitionsForUser(currentUser);
        
        List<PetitionResponseDTO> response = petitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions for user: {}", response.size(), currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Generates pending petitions report - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/pending")
    @Operation(
            summary = "Pending petitions report",
            description = "Generates report of all pending petitions ordered by priority"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending petitions report generated"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getPendingPetitions(HttpSession session) {
        logger.debug("Generating pending petitions report");

       
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para generar reportes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> pendingPetitions = petitionService.searchPetitionsByState(PetitionState.PENDING);
        
       
        pendingPetitions = filterPetitionsByUserAccess(currentUser, pendingPetitions);
        
        List<PetitionResponseDTO> response = pendingPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Generated pending petitions report with {} items for user: {}", response.size(), currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Generates approved petitions report - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/approved")
    @Operation(
            summary = "Approved petitions report",
            description = "Generates report of all approved petitions with complete traceability"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approved petitions report generated"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getApprovedPetitions(HttpSession session) {
        logger.debug("Generating approved petitions report");

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para generar reportes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> approvedPetitions = petitionService.searchPetitionsByState(PetitionState.APPROVED);
        
       
        approvedPetitions = filterPetitionsByUserAccess(currentUser, approvedPetitions);
        
        List<PetitionResponseDTO> response = approvedPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Generated approved petitions report with {} items for user: {}", response.size(), currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Generates rejected petitions report - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/rejected")
    @Operation(
            summary = "Rejected petitions report",
            description = "Generates report of all rejected petitions with rejection reasons"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rejected petitions report generated"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getRejectedPetitions(HttpSession session) {
        logger.debug("Generating rejected petitions report");

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para generar reportes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> rejectedPetitions = petitionService.searchPetitionsByState(PetitionState.REPROVED);
        
      
        rejectedPetitions = filterPetitionsByUserAccess(currentUser, rejectedPetitions);
        
        List<PetitionResponseDTO> response = rejectedPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Generated rejected petitions report with {} items for user: {}", response.size(), currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets petitions by student - Students can only see their own, Deans/VP can see any student.
     */
    @GetMapping("/student/{studentId}")
    @Operation(
            summary = "Get petitions by student",
            description = "Retrieves all petitions for a specific student with complete history"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student petitions retrieved"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to view this student's petitions"),
            @ApiResponse(responseCode = "400", description = "Invalid student ID")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByStudent(
            @Parameter(description = "Student identifier", required = true)
            @PathVariable String studentId,
            HttpSession session) {

        logger.debug("Retrieving petitions for student: {}", studentId);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        
        if (currentUser.getType() == UserType.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new IllegalArgumentException("No puedes ver las solicitudes de otros estudiantes");
        }


        List<Petition> studentPetitions = petitionService.searchPetitionsByStudentId(studentId);
        
        studentPetitions = filterPetitionsByUserAccess(currentUser, studentPetitions);
        
        List<PetitionResponseDTO> response = studentPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions for student: {} by user: {}", response.size(), studentId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets petitions by deanery - DEAN (only their deanery) and ACADEMIC_VICEPRESIDENT (any deanery).
     */
    @GetMapping("/deanery/{deanery}")
    @Operation(
            summary = "Get petitions by deanery",
            description = "Retrieves all petitions assigned to a specific deanery"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deanery petitions retrieved"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByDeanery(
            @Parameter(description = "Deanery name", required = true)
            @PathVariable String deanery,
            HttpSession session) {

        logger.debug("Retrieving petitions for deanery: {}", deanery);

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para ver solicitudes por decanatura");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        validateDeaneryAccess(currentUser, deanery);

        List<Petition> deaneryPetitions = petitionService.searchPetitionsByDeanery(deanery);
        List<PetitionResponseDTO> response = deaneryPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions for deanery: {} by user: {}", response.size(), deanery, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets petitions by type - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/type/{type}")
    @Operation(
            summary = "Get petitions by type",
            description = "Retrieves all petitions of a specific type (ADD_SUBJECT, REMOVE_SUBJECT, CHANGE_GROUP)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Petitions of specified type retrieved"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByType(
            @Parameter(description = "Petition type", required = true)
            @PathVariable PetitionType type,
            HttpSession session) {

        logger.debug("Retrieving petitions of type: {}", type);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para filtrar solicitudes por tipo");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> typePetitions = petitionService.searchPetitionByType(type);
        
       
        typePetitions = filterPetitionsByUserAccess(currentUser, typePetitions);
        
        List<PetitionResponseDTO> response = typePetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions of type: {} for user: {}", response.size(), type, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets petitions by priority - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/priority/{priority}")
    @Operation(
            summary = "Get petitions by priority",
            description = "Retrieves all petitions with a specific priority level"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Petitions of specified priority retrieved"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByPriority(
            @Parameter(description = "Priority level", required = true)
            @PathVariable PetitionPriority priority,
            HttpSession session) {

        logger.debug("Retrieving petitions with priority: {}", priority);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para filtrar solicitudes por prioridad");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> priorityPetitions = petitionService.searchPetitionsByPriority(priority);
        
        
        priorityPetitions = filterPetitionsByUserAccess(currentUser, priorityPetitions);
        
        List<PetitionResponseDTO> response = priorityPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions with priority: {} for user: {}", response.size(), priority, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Changes petition state - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @PutMapping("/{id}/state")
    @Operation(
            summary = "Change petition state",
            description = "Changes the state of a petition and records the decision for traceability"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "State changed successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid state")
    })
    public ResponseEntity<PetitionResponseDTO> changePetitionState(
            @Parameter(description = "Petition ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New petition state", required = true)
            @RequestParam PetitionState state,
            HttpSession session) {

        logger.info("Changing petition state to {} for ID: {}", state, id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        Petition petition = petitionService.searchPetitionsById(id);

        
        validatePetitionAccess(currentUser, petition);

        Petition updatedPetition = petitionService.changePetitionState(id, state);
        PetitionResponseDTO response = convertToResponseDTO(updatedPetition);

        logger.info("Petition state changed successfully for ID: {} by user: {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Changes petition priority - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @PutMapping("/{id}/priority")
    @Operation(
            summary = "Change petition priority",
            description = "Changes the priority of a petition to reorder the processing queue"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Priority changed successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only deans and academic VP"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid priority")
    })
    public ResponseEntity<PetitionResponseDTO> changePetitionPriority(
            @Parameter(description = "Petition ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New petition priority", required = true)
            @RequestParam PetitionPriority priority,
            HttpSession session) {

        logger.info("Changing petition priority to {} for ID: {}", priority, id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                 UserType.DEAN, 
                                                                                 UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para modificar prioridades");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        Petition petition = petitionService.searchPetitionsById(id);

        
        validatePetitionAccess(currentUser, petition);

        Petition updatedPetition = petitionService.changePetitionPriority(id, priority);
        PetitionResponseDTO response = convertToResponseDTO(updatedPetition);

        logger.info("Petition priority changed successfully for ID: {} by user: {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes petition - ACADEMIC_VICEPRESIDENT only.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete petition",
            description = "Deletes a petition from the system (use with caution) - Academic VP only"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Petition deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges - Only academic VP"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")
    })
    public ResponseEntity<Void> deletePetition(
            @Parameter(description = "ID of petition to delete", required = true)
            @PathVariable String id,
            HttpSession session) {

        logger.info("Deleting petition with ID: {}", id);

        
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("Solo el vicepresidente académico puede eliminar solicitudes");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        boolean deleted = petitionService.deletePetition(id);
        if (deleted) {
            logger.info("Petition deleted successfully with ID: {} by user: {}", id, currentUser.getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            logger.warn("Petition not found for deletion with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



    /**
     * Validates if a user can access a specific petition.
     */
    private void validatePetitionAccess(User currentUser, Petition petition) {
        switch (currentUser.getType()) {
            case STUDENT:
                if (!currentUser.getId().equals(petition.getStudentId())) {
                    throw new IllegalArgumentException("No puedes acceder a solicitudes de otros estudiantes");
                }
                break;
            case PROFESSOR:
                throw new IllegalArgumentException("Los profesores no pueden acceder a solicitudes");
            case DEAN:
                
                String userDeanery = getCurrentUserDeanery(currentUser.getId());
                if (userDeanery != null && !userDeanery.equals(petition.getAssociateDeanery())) {
                    throw new IllegalArgumentException("No tienes permisos para acceder a solicitudes de otra decanatura");
                }
                break;
            case ACADEMIC_VICEPRESIDENT:
                
                break;
            default:
                throw new IllegalArgumentException("Tipo de usuario no válido");
        }
    }

    /**
     * Validates if a user can access a specific deanery.
     */
    private void validateDeaneryAccess(User currentUser, String deanery) {
        if (currentUser.getType() == UserType.DEAN) {
            String userDeanery = getCurrentUserDeanery(currentUser.getId());
            if (userDeanery != null && !userDeanery.equals(deanery)) {
                throw new IllegalArgumentException("No tienes permisos para acceder a solicitudes de otra decanatura");
            }
        }
        
    }

    /**
     * Gets petitions based on user permissions.
     */
    private List<Petition> getPetitionsForUser(User user) {
        switch (user.getType()) {
            case DEAN:
                String deanery = getCurrentUserDeanery(user.getId());
                return deanery != null ? 
                    petitionService.searchPetitionsByDeanery(deanery) : 
                    petitionService.searchAllPetitions();
            case ACADEMIC_VICEPRESIDENT:
                return petitionService.searchAllPetitions();
            default:
                throw new IllegalArgumentException("Tipo de usuario no puede ver todas las peticiones");
        }
    }

    /**
     * Filters petitions based on user access permissions.
     */
    private List<Petition> filterPetitionsByUserAccess(User user, List<Petition> petitions) {
        if (user.getType() == UserType.DEAN) {
            String userDeanery = getCurrentUserDeanery(user.getId());
            if (userDeanery != null) {
                return petitions.stream()
                        .filter(petition -> userDeanery.equals(petition.getAssociateDeanery()))
                        .collect(Collectors.toList());
            }
        }
        return petitions; 
    }

    /**
     * Gets the current user's deanery (for deans).
     */
    private String getCurrentUserDeanery(String userId) {
        try {
            Dean dean = deanService.searchDeanById(userId);
            
            if (dean != null && dean.getDeanery() != null) {
                String deaneryName = dean.getDeanery().getDeaneryName();
                logger.debug("Found deanery '{}' for dean with ID: {}", deaneryName, userId);
                return deaneryName;
            } else {
                logger.debug("Dean with ID {} has no deanery assigned", userId);
                return null;
            }
            
        } catch (IllegalArgumentException e) {
            logger.debug("User with ID {} is not a dean or not found: {}", userId, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("Error retrieving deanery for user {}: {}", userId, e.getMessage());
            return null;
        }
    }


    /**
     * Selects the appropriate PetitionCreator based on the request
     */
    private PetitionCreator selectPetitionCreator(PetitionRequestDTO petitionRequest) {
        return petitionCreators.stream()
                .filter(creator -> creator.supports(petitionRequest.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No suitable petition creator found for type: " + petitionRequest.getType()));
    }

    /**
     * Converts a Petition entity to PetitionResponseDTO
     */
    private PetitionResponseDTO convertToResponseDTO(Petition petition) {
        PetitionResponseDTO dto = new PetitionResponseDTO();
        dto.setPetitionId(petition.getPetitionId());
        dto.setStudentId(petition.getStudentId());
        dto.setType(petition.getType());
        dto.setSubjectId(petition.getSubjectId());
        dto.setSubjectShortName(petition.getSubjectShortName());
        dto.setSubjectName(petition.getSubjectName());
        dto.setAssociateDeanery(petition.getAssociateDeanery());
        dto.setPriority(petition.getPriority());
        dto.setState(petition.getState());
        dto.setCreationDate(petition.getCreationDate());
        dto.setModificationDate(petition.getModificationDate());
        dto.setJustification(petition.getJustification());
        dto.setAssignedReviewer(petition.getAssignedReviewer());
        dto.setRejectionReason(petition.getRejectionReason());
        dto.setDecisionHistory(petition.getDecisionHistory());

        dto.setQueuePosition(calculateQueuePosition(petition));

        return dto;
    }

    /**
     * Calculates the queue position of a petition
     */
    private Integer calculateQueuePosition(Petition petition) {
        List<Petition> pendingPetitions = petitionService.searchPetitionsByState(PetitionState.PENDING);
        int position = pendingPetitions.indexOf(petition) + 1;
        return position > 0 ? position : null;
    }


    /*   */

    /**
     * Generates basic petition statistics - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/statistics")
    @Operation(summary = "Estadísticas básicas de solicitudes")
    public ResponseEntity<Map<String, Object>> getPetitionStatistics(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                UserType.DEAN, 
                                                                                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para generar estadísticas");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> allPetitions = getPetitionsForUser(currentUser);
        
        Map<String, Object> stats = generateBasicStatistics(allPetitions);
        return ResponseEntity.ok(stats);
    }

    /**
     * Generates approval rate report - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/approval-rate")
    @Operation(summary = "Tasa de aprobación vs rechazo")
    public ResponseEntity<Map<String, Object>> getApprovalRateReport(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                UserType.DEAN, 
                                                                                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para generar reportes de tasas");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> allPetitions = getPetitionsForUser(currentUser);
        
        Map<String, Object> rates = generateBasicApprovalRates(allPetitions);
        return ResponseEntity.ok(rates);
    }

    /**
     * Generates reassignment statistics - DEAN and ACADEMIC_VICEPRESIDENT only.
     */
    @GetMapping("/reports/reassignment-statistics")
    @Operation(summary = "Estadísticas de reasignaciones por materia")
    public ResponseEntity<Map<String, Object>> getReassignmentStatistics(HttpSession session) {
        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session, 
                                                                                UserType.DEAN, 
                                                                                UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            throw new IllegalArgumentException("No tienes permisos para generar estadísticas de reasignaciones");
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);
        List<Petition> changePetitions = getPetitionsForUser(currentUser).stream()
                .filter(petition -> petition.getType() == PetitionType.CHANGE_GROUP)
                .collect(Collectors.toList());
        
        Map<String, Object> stats = generateBasicReassignmentStats(changePetitions);
        return ResponseEntity.ok(stats);
    }


    /**
     * Generates basic statistics.
     */
    private Map<String, Object> generateBasicStatistics(List<Petition> petitions) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalPetitions", petitions.size());
        
        Map<String, Long> byState = petitions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getState().name(),
                        Collectors.counting()
                ));
        stats.put("byState", byState);
        

        Map<String, Long> byType = petitions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getType().name(),
                        Collectors.counting()
                ));
        stats.put("byType", byType);
        
        stats.put("generatedAt", LocalDateTime.now());
        return stats;
    }

    /**
     * Generates basic approval rates.
     */
    private Map<String, Object> generateBasicApprovalRates(List<Petition> petitions) {
        Map<String, Object> rates = new LinkedHashMap<>();
        
        long total = petitions.size();
        long approved = petitions.stream().filter(p -> p.getState() == PetitionState.APPROVED).count();
        long rejected = petitions.stream().filter(p -> p.getState() == PetitionState.REPROVED).count();
        long pending = petitions.stream().filter(p -> p.getState() == PetitionState.PENDING).count();
        
        rates.put("totalPetitions", total);
        rates.put("approvedCount", approved);
        rates.put("rejectedCount", rejected);
        rates.put("pendingCount", pending);
        
        if (total > 0) {
            rates.put("approvalRate", Math.round((double) approved / total * 100 * 100.0) / 100.0);
            rates.put("rejectionRate", Math.round((double) rejected / total * 100 * 100.0) / 100.0);
            rates.put("pendingRate", Math.round((double) pending / total * 100 * 100.0) / 100.0);
        } else {
            rates.put("approvalRate", 0.0);
            rates.put("rejectionRate", 0.0);
            rates.put("pendingRate", 0.0);
        }
        
        rates.put("generatedAt", LocalDateTime.now());
        return rates;
    }

    /**
     * Generates basic reassignment statistics.
     */
    private Map<String, Object> generateBasicReassignmentStats(List<Petition> changePetitions) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalReassignments", changePetitions.size());
        
        
        Map<String, Long> bySubject = changePetitions.stream()
                .filter(p -> p.getSubjectShortName() != null)
                .collect(Collectors.groupingBy(
                        Petition::getSubjectShortName,
                        Collectors.counting()
                ));
        stats.put("bySubject", bySubject);
        
       
        List<Map<String, Object>> topSubjects = bySubject.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> Map.<String, Object>of(
                        "subject", entry.getKey(),
                        "count", entry.getValue()
                ))
                .collect(Collectors.toList());
        stats.put("topRequestedSubjects", topSubjects);
        
      
        long approvedReassignments = changePetitions.stream()
                .filter(p -> p.getState() == PetitionState.APPROVED)
                .count();
        
        double successRate = changePetitions.isEmpty() ? 0.0 : 
                Math.round((double) approvedReassignments / changePetitions.size() * 100 * 100.0) / 100.0;
        stats.put("successRate", successRate);
        
        stats.put("generatedAt", LocalDateTime.now());
        return stats;
    }

}