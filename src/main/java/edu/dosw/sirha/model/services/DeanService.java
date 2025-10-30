package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.persistence.repository.DeanRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing Dean entities.
 * Handles business logic for CRUD operations related to Deans, who are responsible
 * for leading faculties or schools within the university.
 *
 * Deans oversee academic departments, manage faculty, and represent their respective
 * academic units in university governance.
 */
@Service
public class DeanService {

    private final DeanRepository deanRepository;

    /**
     * Constructor for dependency injection of the DeanRepository.
     *
     * @param deanRepository the repository for Dean data access
     */
    public DeanService(DeanRepository deanRepository) {
        this.deanRepository = deanRepository;
    }

    /**
     * Creates a new Dean record.
     * Generates a unique UUID for the entity and populates it with data from the UserDTO.
     *
     * @param dto the UserDTO containing the dean's basic information (name, email, document)
     * @return the newly created Dean entity with assigned ID
     * @throws IllegalArgumentException if the dto is null or contains invalid data
     */
    public Dean createDean(UserDTO dto) {
        Dean dean = new Dean(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return deanRepository.save(dean);
    }

    public Dean save(Dean dean) {
        return deanRepository.save(dean);
    }

    /**
     * Modifies an existing Dean record.
     * Updates the dean's information while preserving the original ID.
     *
     * @param id the unique identifier of the Dean to modify
     * @param dto the UserDTO containing the updated information
     * @return an Optional containing the updated Dean if found, or empty Optional if not found
     */
    public Optional<Dean> modifyDean(String id, UserDTO dto) {
        return deanRepository.findByDeanCode(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setMail(dto.getMail());
                    existing.setDocument(dto.getDocument());
                    return deanRepository.save(existing);
                });
    }

    /**
     * Deletes a Dean record by its ID.
     *
     * @param id the unique identifier of the Dean to delete
     * @return true if the record was successfully deleted, false if no record was found
     */
    public boolean deleteDean(String id) {
        if (deanRepository.existsById(id)) {
            deanRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Searches for a Dean by their unique identifier.
     *
     * @param code the unique identifier of the Dean to find
     * @return the Dean entity if found
     * @throws IllegalArgumentException if no Dean is found with the given code
     */
    public Dean searchDeanByCode(String code) {
        return deanRepository.findByDeanCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Dean no encontrado con código: " + code));
    }

    /**
     * Retrieves all Dean records from the database.
     * @return a list of all Dean entities
     */
    public List<Dean> searchAllDeans() {
        return deanRepository.findAll();
    }

    /**
     * Searches for Deans associated with a specific Deanery (faculty/school).
     *
     * @param deaneryId the unique identifier of the Deanery
     * @return a list of Deans associated with the specified Deanery
     */
    public List<Dean> searchDeanByDeanery(String deaneryId) {
        return deanRepository.findByDeaneryId(deaneryId);
    }
}