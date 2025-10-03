package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public Professor createProfessor(UserDTO dto) {
        Professor professor = new Professor(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return professorRepository.save(professor);
    }

    public Optional<Professor> modifyProfessor(String id, UserDTO dto) {
        return professorRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setMail(dto.getMail());
                    existing.setDocument(dto.getDocument());
                    return professorRepository.save(existing);
                });
    }

    public boolean deleteProfessor(String id) {
        if (professorRepository.existsById(id)) {
            professorRepository.deleteById(id);
            return true;
        }else {
            return false;

        }
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