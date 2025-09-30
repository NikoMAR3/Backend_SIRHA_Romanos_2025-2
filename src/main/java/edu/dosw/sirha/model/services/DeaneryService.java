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
     */
    public Deanery createDeanery(Deanery deanery) {
        return deaneryRepository.save(deanery);
    }

    /**
     * Modifies an existing deanery.
     * @param deanery the Deanery entity with updated data
     * @return the modified Deanery entity
     */
    public Deanery modifyDeanery(Deanery deanery) {
        return deaneryRepository.save(deanery);
    }

    /**
     * Deletes a deanery by its ID.
     * @param id the ID of the deanery to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteDeanery(String id) {
        if (deaneryRepository.existsById(id)) {
            deaneryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Searches for a deanery by its ID.
     * @param id the ID of the deanery
     * @return the found Deanery entity, or null if it doesn't exist
     */
    public Deanery searchDeaneryById(String id) {
        Optional<Deanery> deanery = deaneryRepository.findDeaneryById(id);
        return deanery.orElse(null);
    }

    /**
     * Gets all deaneries.
     * @return list of all deaneries
     */
    public List<Deanery> searchAllDeaneries() {
        return deaneryRepository.findAll();
    }
}
