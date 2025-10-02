package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public void createProfessor(Professor professor) {
        professorRepository.save(professor);
    }

    public void modifyProfessor(Professor professor) {
        professorRepository.save(professor);
    }

    public void deleteProfessor(String id) {
        if (professorRepository.existsById(id)) {
            professorRepository.deleteById(id);
        }
        // excepcion si no existe ?
    }

    public Professor searchProfessorById(String id) {
        return professorRepository.findById(id).orElse(null);
    }

    public List<Professor> searchAllProfessors() {
        return professorRepository.findAll();
    }

    public List<Professor> searchProfessorsByDeanery(Deanery deanery) {
        return professorRepository.findByDeanery(deanery);
    }

    public List<Professor> searchProfessorsBySubject(Subject subject) {
        return professorRepository.findBySubjectsContaining(subject);
    }
}