package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.AcademicVicePresident;
import edu.dosw.sirha.model.persistence.repository.AcademicVicePresidentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service class for managing Academic Vice President entities.
 * Handles business logic for CRUD operations related to the Academic Vice President role,
 * including creation, modification, deletion, and retrieval of vice president records.
 *
 * The Academic Vice President is a key administrative position responsible for
 * overseeing academic programs and policies at the university level.
 */
@Service
public class AcademicVicePresidentService {

    private final AcademicVicePresidentRepository academicVicePresidentRepository;

    /**
     * Constructor for dependency injection of the AcademicVicePresidentRepository.
     *
     * @param academicVicePresidentRepository the repository for AcademicVicePresident data access
     */
    public AcademicVicePresidentService(AcademicVicePresidentRepository academicVicePresidentRepository){
        this.academicVicePresidentRepository = academicVicePresidentRepository;
    }

    /**
     * Creates a new Academic Vice President record.
     * Generates a unique UUID for the entity and populates it with data from the UserDTO.
     *
     * @param dto the UserDTO containing the vice president's basic information (name, email, document)
     * @return the newly created AcademicVicePresident entity with assigned ID
     * @throws IllegalArgumentException if the dto is null or contains invalid data
     */
    public AcademicVicePresident createAcademicVicePresident(UserDTO dto){
        AcademicVicePresident avp = new AcademicVicePresident(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return academicVicePresidentRepository.save(avp);
    }

    /**
     * Modifies an existing Academic Vice President record.
     * Updates the vice president's information while preserving the original ID.
     *
     * @param id the unique identifier of the Academic Vice President to modify
     * @param dto the UserDTO containing the updated information
     * @return the updated AcademicVicePresident entity, or null if no record was found with the given ID
     */
    public AcademicVicePresident modifyAcademicVicePresident(String id, UserDTO dto){
        return academicVicePresidentRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setMail(dto.getMail());
                    existing.setDocument(dto.getDocument());
                    return academicVicePresidentRepository.save(existing);
                })
                .orElse(null);
    }

    /**
     * Deletes an Academic Vice President record by its ID.
     *
     * @param avpId the unique identifier of the Academic Vice President to delete
     * @return true if the record was successfully deleted, false if no record was found with the given ID
     */
    public boolean deleteAcademicVicePresident(String avpId) {
        if (academicVicePresidentRepository.existsById(avpId)) {
            academicVicePresidentRepository.deleteById(avpId);
            return true;
        }
        return false;
    }

    /**
     * Searches for an Academic Vice President by their unique identifier.
     *
     * @param avpId the unique identifier of the Academic Vice President to find
     * @return the AcademicVicePresident entity if found, or null if no record was found
     */
    public AcademicVicePresident searchAcademicVicePresidentById(String avpId){
        return academicVicePresidentRepository.findById(avpId).orElse(null);
    }
}