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
     * @throws IllegalArgumentException if subject is null or has invalid data
     */
    public Subject createSubject(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }

        validateSubjectData(subject);

        Optional<Subject> existingSubject = subjectRepository.findByShortName(subject.getShortName());
        if (existingSubject.isPresent()) {
            throw new IllegalArgumentException("Subject with short name '" + subject.getShortName() + "' already exists");
        }

        return subjectRepository.save(subject);
    }

    /**
     * Modifies an existing subject.
     * @param subject the Subject entity with updated data
     * @return the modified Subject entity
     * @throws IllegalArgumentException if subject is null, has invalid data, or doesn't exist
     */
    public Subject modifySubject(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        if (subject.getId() == null || subject.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty for modification");
        }

        validateSubjectData(subject);

        if (!subjectRepository.existsById(subject.getId())) {
            throw new IllegalArgumentException("Subject with ID '" + subject.getId() + "' does not exist");
        }

        return subjectRepository.save(subject);
    }

    /**
     * Deletes a subject by its ID.
     * @param id the ID of the subject to delete
     * @throws IllegalArgumentException if ID is null or empty, or subject doesn't exist
     */
    public void deleteSubject(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty");
        }

        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject with ID '" + id + "' does not exist");
        }

        subjectRepository.deleteById(id);
    }

    /**
     * Searches for a subject by its ID.
     * @param id the ID of the subject
     * @return the found Subject entity
     * @throws IllegalArgumentException if ID is null or empty, or subject doesn't exist
     */
    public Subject searchSubjectById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty");
        }

        Optional<Subject> subject = subjectRepository.findById(id);
        return subject.orElseThrow(() ->
                new IllegalArgumentException("Subject with ID '" + id + "' not found"));
    }

    /**
     * Searches for a subject by its short name.
     * @param shortName the short name of the subject
     * @return the found Subject entity
     * @throws IllegalArgumentException if short name is null or empty, or subject doesn't exist
     */
    public Subject searchSubjectByShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name cannot be null or empty");
        }

        Optional<Subject> subject = subjectRepository.findByShortName(shortName);
        return subject.orElseThrow(() ->
                new IllegalArgumentException("Subject with short name '" + shortName + "' not found"));
    }

    /**
     * Searches for subjects by academic program.
     * @param program the academic program
     * @return list of subjects found for the program
     * @throws IllegalArgumentException if program is null or program ID is invalid
     */
    public List<Subject> searchSubjectsByProgram(AcademicProgram program) {
        if (program == null) {
            throw new IllegalArgumentException("Academic program cannot be null");
        }
        if (program.getId() == null || program.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Academic program ID cannot be null or empty");
        }

        return subjectRepository.findByProgramId(program.getId());
    }

    /**
     * Retrieves all subjects.
     * @return list of all subjects
     */
    public List<Subject> searchAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Searches for subjects by number of credits.
     * @param credits the number of credits
     * @return list of subjects with the specified credits
     * @throws IllegalArgumentException if credits is invalid
     */
    public List<Subject> searchSubjectsByCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be greater than 0");
        }

        return subjectRepository.findByCredits(credits);
    }

    /**
     * Searches for subjects by academic level.
     * @param level the academic level
     * @return list of subjects at the specified level
     * @throws IllegalArgumentException if level is invalid
     */
    public List<Subject> searchSubjectsByLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("Level must be greater than 0");
        }

        return subjectRepository.findByLevel(level);
    }

    /**
     * Searches for the prerequisites of a subject by its ID.
     * @param id the ID of the subject
     * @return list of prerequisite subjects
     * @throws IllegalArgumentException if ID is null or empty, or subject doesn't exist
     */
    public List<Subject> searchSubjectPreRequisites(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty");
        }

        Subject subject = searchSubjectById(id);
        return subject.getPrerequisites() != null ? subject.getPrerequisites() : List.of();
    }

    /**
     * Checks if a subject exists by its ID.
     * @param id the ID of the subject
     * @return true if the subject exists, false otherwise
     * @throws IllegalArgumentException if ID is null or empty
     */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty");
        }

        return subjectRepository.existsById(id);
    }

    /**
     * Validates subject data.
     * @param subject the subject to validate
     * @throws IllegalArgumentException if subject data is invalid
     */
    private void validateSubjectData(Subject subject) {
        if (subject.getShortName() == null || subject.getShortName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject short name cannot be null or empty");
        }
        if (subject.getName() == null || subject.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be null or empty");
        }
        if (subject.getCredits() <= 0) {
            throw new IllegalArgumentException("Subject credits must be greater than 0");
        }
        if (subject.getLevel() <= 0) {
            throw new IllegalArgumentException("Subject level must be greater than 0");
        }
    }

    public Subject save(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        validateSubjectData(subject);
        return subjectRepository.save(subject);
    }

}
