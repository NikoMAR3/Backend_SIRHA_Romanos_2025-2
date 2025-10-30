package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeaneryService {

    private final DeaneryRepository deaneryRepository;

    public DeaneryService(DeaneryRepository deaneryRepository) {
        this.deaneryRepository = deaneryRepository;
    }

    /**
     * Creates a new deanery in the database.
     * @param deanery the Deanery entity to create
     * @return the saved Deanery entity
     * @throws IllegalArgumentException if deanery is null or has invalid data
     */
    public Deanery createDeanery(Deanery deanery) {
        if (deanery == null) {
            throw new IllegalArgumentException("Deanery cannot be null");
        }
        if (deanery.getDeaneryName() == null || deanery.getDeaneryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery name cannot be null or empty");
        }

        Optional<Deanery> existingDeanery = deaneryRepository.findByDeaneryName(deanery.getDeaneryName());
        if (existingDeanery.isPresent()) {
            throw new IllegalArgumentException("Deanery with name '" + deanery.getDeaneryName() + "' already exists");
        }

        return deaneryRepository.save(deanery);
    }

    /**
     * Modifies an existing deanery.
     * @param deanery the Deanery entity with updated data
     * @return the modified Deanery entity
     * @throws IllegalArgumentException if deanery is null, has invalid data, or doesn't exist
     */
    public Deanery modifyDeanery(Deanery deanery) {
        if (deanery == null) {
            throw new IllegalArgumentException("Deanery cannot be null");
        }
        if (deanery.getId() == null || deanery.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery ID cannot be null or empty for modification");
        }
        if (deanery.getDeaneryName() == null || deanery.getDeaneryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery name cannot be null or empty");
        }

        if (!deaneryRepository.existsById(deanery.getId())) {
            throw new IllegalArgumentException("Deanery with ID '" + deanery.getId() + "' does not exist");
        }

        return deaneryRepository.save(deanery);
    }

    /**
     * Deletes a deanery by its ID.
     * @param id the ID of the deanery to delete
     * @throws IllegalArgumentException if ID is null or empty, or deanery doesn't exist
     */
    public void deleteDeanery(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery ID cannot be null or empty");
        }

        if (!deaneryRepository.existsById(id)) {
            throw new IllegalArgumentException("Deanery with ID '" + id + "' does not exist");
        }

        deaneryRepository.deleteById(id);
    }

    /**
     * Searches for a deanery by its ID.
     * @param id the ID of the deanery
     * @return the found Deanery entity
     * @throws IllegalArgumentException if ID is null or empty, or deanery doesn't exist
     */
    public Deanery searchDeaneryById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery ID cannot be null or empty");
        }

        Optional<Deanery> deanery = deaneryRepository.findDeaneryById(id);
        return deanery.orElseThrow(() ->
                new IllegalArgumentException("Deanery with ID '" + id + "' not found"));
    }

    /**
     * Searches for a deanery by its name.
     * @param name the name of the deanery
     * @return the found Deanery entity
     * @throws IllegalArgumentException if name is null or empty, or deanery doesn't exist
     */
    public Deanery searchDeaneryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery name cannot be null or empty");
        }

        Optional<Deanery> deanery = deaneryRepository.findByDeaneryName(name);
        return deanery.orElseThrow(() ->
                new IllegalArgumentException("Deanery with name '" + name + "' not found"));
    }

    /**
     * Gets all deaneries.
     * @return list of all deaneries
     */
    public List<Deanery> searchAllDeaneries() {
        return deaneryRepository.findAll();
    }

    /**
     * Checks if a deanery exists by its ID.
     * @param id the ID of the deanery
     * @return true if the deanery exists, false otherwise
     * @throws IllegalArgumentException if ID is null or empty
     */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Deanery ID cannot be null or empty");
        }

        return deaneryRepository.existsById(id);
    }
}