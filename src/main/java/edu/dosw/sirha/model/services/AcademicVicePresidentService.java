package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.AcademicVicePresident;
import edu.dosw.sirha.model.persistence.repository.AcademicVicePresidentRepository;
import org.springframework.stereotype.Service;

@Service
public class AcademicVicePresidentService {

    private AcademicVicePresidentRepository academicVicePresidentRepository;

    public AcademicVicePresidentService(AcademicVicePresidentRepository academicVicePresidentRepository){
        this.academicVicePresidentRepository = academicVicePresidentRepository;
    }

    public void createAcademicVicePresident(AcademicVicePresident academicVicePresident){
        academicVicePresidentRepository.save(academicVicePresident);
    }

    public void modifyAcademicVicePresident(AcademicVicePresident avp){
        academicVicePresidentRepository.save(avp);
    }

    public void deleteAcademicVicePresident(String avpId){
        if(academicVicePresidentRepository.existsById(avpId)){
            academicVicePresidentRepository.deleteById(avpId);
        }
    }

    public AcademicVicePresident searchAcademicVicePresident(String avpId){
        return academicVicePresidentRepository.findById(avpId).orElse(null);
        //deberia ser or else throw exception
    }
}
