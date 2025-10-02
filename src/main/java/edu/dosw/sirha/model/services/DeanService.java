package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.persistence.repository.DeanRepository;
import org.springframework.stereotype.Service;
import java.util.List;

    @Service
    public class DeanService {

        private final DeanRepository deanRepository;

        public DeanService(DeanRepository deanRepository) {
            this.deanRepository = deanRepository;
        }

        public Dean createDean(Dean dean) {
            return deanRepository.save(dean);
        }

        public Dean modifyDean(Dean dean) {
            if (!deanRepository.existsById(dean.getId())) {
                throw new IllegalArgumentException("Dean no encontrado con id: " + dean.getId());
            }
            return deanRepository.save(dean);
        }

        public boolean deleteDean(String id) {
            if (deanRepository.existsById(id)) {
                deanRepository.deleteById(id);
                return true;
            }
            return false;
        }

        public Dean searchDeanById(String id) {
            return deanRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Dean no encontrado con id: " + id));
        }

        public List<Dean> searchAllDeans() {
            return deanRepository.findAll();
        }

        public List<Dean> searchDeanByDeanery(String deaneryId) {
            return deanRepository.findByDeaneryId(deaneryId);
        }
    }

