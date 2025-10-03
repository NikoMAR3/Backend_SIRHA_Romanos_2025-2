package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.AcademicVicePresident;
import edu.dosw.sirha.model.persistence.repository.AcademicVicePresidentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AcademicVicePresidentService {

    private final AcademicVicePresidentRepository academicVicePresidentRepository;

    public AcademicVicePresidentService(AcademicVicePresidentRepository academicVicePresidentRepository){
        this.academicVicePresidentRepository = academicVicePresidentRepository;
    }

    public AcademicVicePresident createAcademicVicePresident(UserDTO dto){
        AcademicVicePresident avp = new AcademicVicePresident(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return academicVicePresidentRepository.save(avp);
    }

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

    public boolean deleteAcademicVicePresident(String avpId) {
        if (academicVicePresidentRepository.existsById(avpId)) {
            academicVicePresidentRepository.deleteById(avpId);
            return true;
        }
        return false;
    }

    public AcademicVicePresident searchAcademicVicePresidentById(String avpId){
        return academicVicePresidentRepository.findById(avpId).orElse(null);
    }
}
