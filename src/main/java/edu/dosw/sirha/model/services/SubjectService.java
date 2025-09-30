package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /**
     * Creates a new subject in the database.
     * @param subject the Subject entity to create
     * @return the saved Subject entity
     */
    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    /**
     * Modifies an existing subject.
     * @param subject the Subject entity with updated data
     * @return the modified Subject entity
     */
    public Subject modifySubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    /**
     * Deletes a subject by its ID.
     * @param id the ID of the subject to delete
     */
    public void deleteSubject(String id) {
        subjectRepository.deleteById(id);
    }

    /**
     * Searches for a subject by its ID.
     * @param id the ID of the subject
     * @return the found Subject entity, or null if not found
     */
    public Subject searchSubjectById(String id) {
        Optional<Subject> subject = subjectRepository.findById(id);
        return subject.orElse(null);
    }

    /**
     * Searches for a subject by academic program.
     * @param program the academic program
     * @return the found Subject entity, or null if not found
     */
    public Subject searchSubjectByProgram(AcademicProgram program) {
        List<Subject> subjects = subjectRepository.findByProgramId(program.getId());
        return subjects.isEmpty() ? null : subjects.get(0);
    }

    /**
     * Retrieves all subjects.
     * @return list of all subjects
     */
    public List<Subject> searchAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Searches for a subject by number of credits.
     * @param credits the number of credits
     * @return the found Subject entity, or null if not found
     */
    public Subject searchSubjectByCredits(int credits) {
        List<Subject> subjects = subjectRepository.findByCredits(credits);
        return subjects.isEmpty() ? null : subjects.get(0);
    }

    /**
     * Searches for a subject by academic level.
     * @param level the academic level
     * @return the found Subject entity, or null if not found
     */
    public Subject searchSubjectByLevel(int level) {
        List<Subject> subjects = subjectRepository.findByLevel(level);
        return subjects.isEmpty() ? null : subjects.get(0);
    }

    /**
     * Searches for the prerequisites of a subject by its ID.
     * @param id the ID of the subject
     * @return list of prerequisite subjects
     */
    public List<Subject> searchSubjectPreRequisites(String id) {
        Optional<Subject> subject = subjectRepository.findById(id);
        return subject.map(Subject::getPrerequisites).orElse(List.of());
    }
}
