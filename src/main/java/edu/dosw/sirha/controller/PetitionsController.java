package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.controller.dtos.PetitionResponseDTO;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.services.PetitionService;
import edu.dosw.sirha.model.components.util.PetitionCreator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/petitions")
@Tag(name = "Petition Management", description = "API for comprehensive management of academic petitions with automatic routing and traceability")
public class PetitionsController {

    private static final Logger logger = LoggerFactory.getLogger(PetitionsController.class);

    private final PetitionService petitionService;
    private final PetitionCreator petitionCreatorFactory;

    @Autowired
    public PetitionsController(PetitionService petitionService, PetitionCreator petitionCreatorFactory) {
        this.petitionService = petitionService;
        this.petitionCreatorFactory = petitionCreatorFactory;
    }

    @PostMapping
    @Operation(
            summary = "Create new petition",
            description = "Receives and creates a new petition with automatic routing by faculty and automatic priority assignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Petition created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid petition data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PetitionResponseDTO> createPetition(
            @Valid @RequestBody PetitionRequestDTO petitionRequest) {

        logger.info("Creating new petition for student: {}", petitionRequest.getUserID());

        Petition petition = petitionCreatorFactory.createPetition(petitionRequest);
        Petition savedPetition = petitionService.createPetition(petition);
        PetitionResponseDTO response = convertToResponseDTO(savedPetition);

        logger.info("Petition created successfully with ID: {}", savedPetition.getPetitionId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get petition by ID",
            description = "Retrieves a specific petition with all its information and traceability"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Petition found"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")
    })
    public ResponseEntity<PetitionResponseDTO> getPetitionById(
            @Parameter(description = "Unique petition identifier", required = true)
            @PathVariable String id) {

        logger.debug("Retrieving petition with ID: {}", id);

        Petition petition = petitionService.searchPetitionsById(id);
        PetitionResponseDTO response = convertToResponseDTO(petition);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "List all petitions",
            description = "Retrieves all petitions in the system ordered by creation date"
    )
    @ApiResponse(responseCode = "200", description = "List of petitions retrieved successfully")
    public ResponseEntity<List<PetitionResponseDTO>> getAllPetitions() {
        logger.debug("Retrieving all petitions");

        List<Petition> petitions = petitionService.searchAllPetitions();
        List<PetitionResponseDTO> response = petitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/pending")
    @Operation(
            summary = "Pending petitions report",
            description = "Generates report of all pending petitions ordered by priority"
    )
    @ApiResponse(responseCode = "200", description = "Pending petitions report generated")
    public ResponseEntity<List<PetitionResponseDTO>> getPendingPetitions() {
        logger.debug("Generating pending petitions report");

        List<Petition> pendingPetitions = petitionService.searchPetitionsByState(PetitionState.PENDING);
        List<PetitionResponseDTO> response = pendingPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Generated pending petitions report with {} items", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/approved")
    @Operation(
            summary = "Approved petitions report",
            description = "Generates report of all approved petitions with complete traceability"
    )
    @ApiResponse(responseCode = "200", description = "Approved petitions report generated")
    public ResponseEntity<List<PetitionResponseDTO>> getApprovedPetitions() {
        logger.debug("Generating approved petitions report");

        List<Petition> approvedPetitions = petitionService.searchPetitionsByState(PetitionState.APPROVED);
        List<PetitionResponseDTO> response = approvedPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Generated approved petitions report with {} items", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/rejected")
    @Operation(
            summary = "Rejected petitions report",
            description = "Generates report of all rejected petitions with rejection reasons"
    )
    @ApiResponse(responseCode = "200", description = "Rejected petitions report generated")
    public ResponseEntity<List<PetitionResponseDTO>> getRejectedPetitions() {
        logger.debug("Generating rejected petitions report");

        List<Petition> rejectedPetitions = petitionService.searchPetitionsByState(PetitionState.REPROVED);
        List<PetitionResponseDTO> response = rejectedPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Generated rejected petitions report with {} items", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @Operation(
            summary = "Get petitions by student",
            description = "Retrieves all petitions for a specific student with complete history"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student petitions retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid student ID")
    })
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByStudent(
            @Parameter(description = "Student identifier", required = true)
            @PathVariable String studentId) {

        logger.debug("Retrieving petitions for student: {}", studentId);

        List<Petition> studentPetitions = petitionService.searchPetitionsByStudentId(studentId);
        List<PetitionResponseDTO> response = studentPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions for student: {}", response.size(), studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deanery/{deanery}")
    @Operation(
            summary = "Get petitions by deanery",
            description = "Retrieves all petitions assigned to a specific deanery"
    )
    @ApiResponse(responseCode = "200", description = "Deanery petitions retrieved")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByDeanery(
            @Parameter(description = "Deanery name", required = true)
            @PathVariable String deanery) {

        logger.debug("Retrieving petitions for deanery: {}", deanery);

        List<Petition> deaneryPetitions = petitionService.searchPetitionsByDeanery(deanery);
        List<PetitionResponseDTO> response = deaneryPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions for deanery: {}", response.size(), deanery);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    @Operation(
            summary = "Get petitions by type",
            description = "Retrieves all petitions of a specific type (ADD_SUBJECT, REMOVE_SUBJECT, CHANGE_GROUP)"
    )
    @ApiResponse(responseCode = "200", description = "Petitions of specified type retrieved")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByType(
            @Parameter(description = "Petition type", required = true)
            @PathVariable PetitionType type) {

        logger.debug("Retrieving petitions of type: {}", type);

        List<Petition> typePetitions = petitionService.searchPetitionByType(type);
        List<PetitionResponseDTO> response = typePetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions of type: {}", response.size(), type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/priority/{priority}")
    @Operation(
            summary = "Get petitions by priority",
            description = "Retrieves all petitions with a specific priority level"
    )
    @ApiResponse(responseCode = "200", description = "Petitions of specified priority retrieved")
    public ResponseEntity<List<PetitionResponseDTO>> getPetitionsByPriority(
            @Parameter(description = "Priority level", required = true)
            @PathVariable PetitionPriority priority) {

        logger.debug("Retrieving petitions with priority: {}", priority);

        List<Petition> priorityPetitions = petitionService.searchPetitionsByPriority(priority);
        List<PetitionResponseDTO> response = priorityPetitions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        logger.info("Retrieved {} petitions with priority: {}", response.size(), priority);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/state")
    @Operation(
            summary = "Change petition state",
            description = "Changes the state of a petition and records the decision for traceability"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "State changed successfully"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid state")
    })
    public ResponseEntity<PetitionResponseDTO> changePetitionState(
            @Parameter(description = "Petition ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New petition state", required = true)
            @RequestParam PetitionState state) {

        logger.info("Changing petition state to {} for ID: {}", state, id);

        Petition updatedPetition = petitionService.changePetitionState(id, state);
        PetitionResponseDTO response = convertToResponseDTO(updatedPetition);

        logger.info("Petition state changed successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/priority")
    @Operation(
            summary = "Change petition priority",
            description = "Changes the priority of a petition to reorder the processing queue"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Priority changed successfully"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid priority")
    })
    public ResponseEntity<PetitionResponseDTO> changePetitionPriority(
            @Parameter(description = "Petition ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New petition priority", required = true)
            @RequestParam PetitionPriority priority) {

        logger.info("Changing petition priority to {} for ID: {}", priority, id);

        Petition updatedPetition = petitionService.changePetitionPriority(id, priority);
        PetitionResponseDTO response = convertToResponseDTO(updatedPetition);

        logger.info("Petition priority changed successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete petition",
            description = "Deletes a petition from the system (use with caution)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Petition deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Petition not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")
    })
    public ResponseEntity<Void> deletePetition(
            @Parameter(description = "ID of petition to delete", required = true)
            @PathVariable String id) {

        logger.info("Deleting petition with ID: {}", id);

        boolean deleted = petitionService.deletePetition(id);
        if (deleted) {
            logger.info("Petition deleted successfully with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            logger.warn("Petition not found for deletion with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Converts a Petition entity to PetitionResponseDTO
     * @param petition the petition entity to convert
     * @return converted PetitionResponseDTO
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
     * @param petition the petition to calculate position for
     * @return the queue position
     */
    private Integer calculateQueuePosition(Petition petition) {
        List<Petition> pendingPetitions = petitionService.searchPetitionsByState(PetitionState.PENDING);
        int position = pendingPetitions.indexOf(petition) + 1;
        return position > 0 ? position : null;
    }
}
