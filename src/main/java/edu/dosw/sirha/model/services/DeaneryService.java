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
     * Crea una nueva decanatura en la base de datos.
     * @param deanery la entidad Deanery a crear
     * @return la entidad Deanery guardada
     */
    public Deanery createDeanery(Deanery deanery) {
        return deaneryRepository.save(deanery);
    }

    /**
     * Modifica una decanatura existente.
     * @param deanery la entidad Deanery con los datos actualizados
     * @return la entidad Deanery modificada
     */
    public Deanery modifyDeanery(Deanery deanery) {
        return deaneryRepository.save(deanery);
    }

    /**
     * Elimina una decanatura por su ID.
     * @param id el ID de la decanatura a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deleteDeanery(String id) {
        if (deaneryRepository.existsById(id)) {
            deaneryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Busca una decanatura por su ID.
     * @param id el ID de la decanatura
     * @return la entidad Deanery encontrada, o null si no existe
     */
    public Deanery searchDeaneryById(String id) {
        Optional<Deanery> deanery = deaneryRepository.findDeaneryById(id);
        return deanery.orElse(null);
    }

    /**
     * Obtiene todas las decanaturas.
     * @return lista de todas las decanaturas
     */
    public List<Deanery> searchAllDeaneries() {
        return deaneryRepository.findAll();
    }
}
