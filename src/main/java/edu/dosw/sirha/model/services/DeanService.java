package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.persistence.repository.DeanRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
    public class DeanService {

        private final DeanRepository deanRepository;

        public DeanService(DeanRepository deanRepository) {
            this.deanRepository = deanRepository;
        }

    public Dean createDean(UserDTO dto) {
        Dean dean = new Dean(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return deanRepository.save(dean);
    }


    public Optional<Dean> modifyDean(String id, UserDTO dto) {
            return deanRepository.findById(id)
                    .map(existing -> {
                        existing.setName(dto.getName());
                        existing.setMail(dto.getMail());
                        existing.setDocument(dto.getDocument());
                        return deanRepository.save(existing);
                    });
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

