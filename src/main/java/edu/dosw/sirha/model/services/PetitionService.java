package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.components.util.PetitionHandler;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.persistence.repository.PetitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing petition operations.
 * Provides comprehensive CRUD operations and advanced search functionality
 * for the academic petition management system.
 */
@Service
public class PetitionService {

    private static final Logger logger = LoggerFactory.getLogger(PetitionService.class);

    private final PetitionRepository petitionRepository;
    private final PetitionHandler petitionHandlerChain;

    public PetitionService(PetitionRepository petitionRepository, PetitionHandler petitionHandlerChain) {
        this.petitionRepository = petitionRepository;
        this.petitionHandlerChain = petitionHandlerChain;
    }

    /**
     * Creates a new petition in the system.
     * @param petition the petition to create
     * @return the created petition with generated ID and timestamps
     * @throws IllegalArgumentException if petition is null or has invalid data
     */
    public Petition createPetition(Petition petition) {
        if (petition == null) {
            throw new IllegalArgumentException("Petition cannot be null");
        }

        validatePetitionForCreation(petition);

        petition.setCreationDate(LocalDateTime.now());
        petition.setModificationDate(LocalDateTime.now());
        petition.setState(PetitionState.PENDING);

        Petition savedPetition = petitionRepository.save(petition);
        logger.info("Petition created successfully with ID: {}", savedPetition.getPetitionId());

        petitionHandlerChain.answerPetition(savedPetition);

        return savedPetition;
    }

    /**
     * Modifies an existing petition with new data.
     * @param petition the petition with updated information
     * @return the modified petition
     * @throws IllegalArgumentException if petition is null, ID is missing, or petition doesn't exist
     */
    public Petition modifyPetition(Petition petition) {
        if (petition == null) {
            throw new IllegalArgumentException("Petition cannot be null");
        }

        if (petition.getPetitionId() == null || petition.getPetitionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Petition ID is required for modification");
        }

        if (!petitionRepository.existsById(petition.getPetitionId())) {
            throw new IllegalArgumentException("Petition with ID '" + petition.getPetitionId() + "' not found");
        }

        validatePetitionForModification(petition);

        petition.setModificationDate(LocalDateTime.now());

        Petition modifiedPetition = petitionRepository.save(petition);
        logger.info("Petition modified successfully with ID: {}", modifiedPetition.getPetitionId());

        return modifiedPetition;
    }

    /**
     * Deletes a petition by its ID.
     * @param id the unique identifier of the petition to delete
     * @return true if the petition was successfully deleted, false if not found
     * @throws IllegalArgumentException if ID is null or empty
     */
    public boolean deletePetition(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Petition ID cannot be null or empty");
        }

        if (!petitionRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent petition with ID: {}", id);
            return false;
        }

        petitionRepository.deleteById(id);
        logger.info("Petition deleted successfully with ID: {}", id);
        return true;
    }

    /**
     * Searches for a petition by its unique identifier.
     * @param id the unique identifier of the petition
     * @return the petition if found
     * @throws IllegalArgumentException if ID is null, empty, or petition not found
     */
    public Petition searchPetitionsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Petition ID cannot be null or empty");
        }

        Optional<Petition> petition = petitionRepository.findById(id);
        if (petition.isEmpty()) {
            throw new IllegalArgumentException("Petition with ID '" + id + "' not found");
        }

        logger.debug("Petition found with ID: {}", id);
        return petition.get();
    }

    /**
     * Retrieves all petitions in the system.
     * @return list of all petitions, ordered by creation date (newest first)
     */
    public List<Petition> searchAllPetitions() {
        List<Petition> petitions = petitionRepository.findAllByOrderByCreationDateDesc();
        logger.info("Retrieved {} petitions from database", petitions.size());
        return petitions;
    }

    /**
     * Searches for petitions by their type.
     * @param type the petition type to search for
     * @return list of petitions matching the specified type
     * @throws IllegalArgumentException if type is null
     */
    public List<Petition> searchPetitionByType(PetitionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Petition type cannot be null");
        }

        List<Petition> petitions = petitionRepository.findByType(type);
        logger.debug("Found {} petitions of type: {}", petitions.size(), type);
        return petitions;
    }

    /**
     * Changes the state of a petition.
     * @param petitionId the unique identifier of the petition
     * @param newState the new state to assign to the petition
     * @return the updated petition
     * @throws IllegalArgumentException if parameters are invalid or petition not found
     */
    public Petition changePetitionState(String petitionId, PetitionState newState) {
        if (petitionId == null || petitionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Petition ID cannot be null or empty");
        }
        if (newState == null) {
            throw new IllegalArgumentException("New state cannot be null");
        }

        Petition petition = searchPetitionsById(petitionId);
        PetitionState oldState = petition.getState();

        petition.setState(newState);
        petition.setModificationDate(LocalDateTime.now());

        Petition updatedPetition = petitionRepository.save(petition);
        logger.info("Petition state changed from {} to {} for ID: {}", oldState, newState, petitionId);

        return updatedPetition;
    }

    /**
     * Changes the priority of a petition.
     * @param petitionId the unique identifier of the petition
     * @param priority the new priority to assign to the petition
     * @return the updated petition
     * @throws IllegalArgumentException if parameters are invalid or petition not found
     */
    public Petition changePetitionPriority(String petitionId, PetitionPriority priority) {
        if (petitionId == null || petitionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Petition ID cannot be null or empty");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }

        Petition petition = searchPetitionsById(petitionId);
        PetitionPriority oldPriority = petition.getPriority();

        petition.setPriority(priority);
        petition.setModificationDate(LocalDateTime.now());

        Petition updatedPetition = petitionRepository.save(petition);
        logger.info("Petition priority changed from {} to {} for ID: {}", oldPriority, priority, petitionId);

        return updatedPetition;
    }

    /**
     * Searches for petitions by their priority level.
     * @param priority the priority level to search for
     * @return list of petitions matching the specified priority
     * @throws IllegalArgumentException if priority is null
     */
    public List<Petition> searchPetitionsByPriority(PetitionPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }

        List<Petition> petitions = petitionRepository.findByPriority(priority);
        logger.debug("Found {} petitions with priority: {}", petitions.size(), priority);
        return petitions;
    }

    /**
     * Searches for petitions created on or after a specific date.
     * @param date the creation date threshold
     * @return list of petitions created on or after the specified date
     * @throws IllegalArgumentException if date is null
     */
    public List<Petition> searchPetitionsByCreationDate(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        List<Petition> petitions = petitionRepository.findByCreationDateAfter(date);
        logger.debug("Found {} petitions created after: {}", petitions.size(), date);
        return petitions;
    }

    /**
     * Searches for petitions by their current state.
     * @param state the petition state to search for
     * @return list of petitions matching the specified state
     * @throws IllegalArgumentException if state is null
     */
    public List<Petition> searchPetitionsByState(PetitionState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        List<Petition> petitions = petitionRepository.findByState(state);
        logger.debug("Found {} petitions with state: {}", petitions.size(), state);
        return petitions;
    }

    /**
     * Searches for petitions by associated deanery.
     * @param deanery the deanery name or identifier
     * @return list of petitions associated with the specified deanery
     * @throws IllegalArgumentException if deanery is null or empty
     */
    public List<Petition> searchPetitionsByDeanery(String deanery) {
        if (deanery == null || deanery.trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery cannot be null or empty");
        }

        List<Petition> petitions = petitionRepository.findByDeanery(deanery);
        logger.debug("Found {} petitions for deanery: {}", petitions.size(), deanery);
        return petitions;
    }

    /**
     * Searches for petitions submitted by a specific student.
     * @param studentId the unique identifier of the student
     * @return list of petitions submitted by the specified student
     * @throws IllegalArgumentException if studentId is null or empty
     */
    public List<Petition> searchPetitionsByStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        List<Petition> petitions = petitionRepository.findByStudentId(studentId);
        logger.debug("Found {} petitions for student: {}", petitions.size(), studentId);
        return petitions;
    }

    /**
     * Searches for petitions related to a specific subject.
     * @param subjectId the unique identifier of the subject
     * @return list of petitions related to the specified subject
     * @throws IllegalArgumentException if subjectId is null or empty
     */
    public List<Petition> searchPetitionsBySubjectId(String subjectId) {
        if (subjectId == null || subjectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty");
        }

        List<Petition> petitions = petitionRepository.findBySubjectId(subjectId);
        logger.debug("Found {} petitions for subject: {}", petitions.size(), subjectId);
        return petitions;
    }

    /**
     * Validates petition data for creation.
     * @param petition the petition to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePetitionForCreation(Petition petition) {
        if (petition.getStudentId() == null || petition.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (petition.getType() == null) {
            throw new IllegalArgumentException("Petition type is required");
        }
        if (petition.getPriority() == null) {
            throw new IllegalArgumentException("Petition priority is required");
        }
        if (petition.getJustification() == null || petition.getJustification().trim().isEmpty()) {
            throw new IllegalArgumentException("Justification is required");
        }
    }

    /**
     * Validates petition data for modification.
     * @param petition the petition to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePetitionForModification(Petition petition) {
        if (petition.getType() == null) {
            throw new IllegalArgumentException("Petition type cannot be null");
        }
        if (petition.getState() == null) {
            throw new IllegalArgumentException("Petition state cannot be null");
        }
        if (petition.getPriority() == null) {
            throw new IllegalArgumentException("Petition priority cannot be null");
        }
    }
}
