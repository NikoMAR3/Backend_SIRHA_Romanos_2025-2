package edu.dosw.sirha.model.persistence.repository;


import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.AcademicProgram;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Subject} entities in MongoDB.
*/
@Repository
public interface SubjectRepository extends MongoRepository<Subject, String> {

    /**
     * Finds a subject by its short name (abbreviation).
     *
     * @param shortName the short name of the subject
     * @return an {@link Optional} containing the subject if found, otherwise empty
     */
    Optional<Subject> findByShortName(String shortName);

    /**
     * Finds a subject by its full name.
     *
     * @param name the full descriptive name of the subject
     * @return an {@link Optional} containing the subject if found, otherwise empty
     */
    Optional<Subject> findByName(String name);

    /**
     * Retrieves all subjects that belong to a specific academic level.
     *
     * @param level the academic level (e.g., semester or course level)
     * @return a list of subjects at the given level
     */
    List<Subject> findByLevel(int level);

    /**
     * Retrieves all subjects with the specified number of credits.
     *
     * @param credits the number of credits to search for
     * @return a list of subjects with the specified credits
     */
    List<Subject> findByCredits(int credits);

    /**
     * Retrieves all subjects that have at least the specified number of credits.
     *
     * @param credits the minimum number of credits
     * @return a list of subjects with credits greater than or equal to the specified value
     */
    List<Subject> findByCreditsGreaterThanEqual(int credits);

    /**
     * Finds all subjects belonging to a specific academic program.
     *
     * @param programId the academic program to which the subject belongs
     * @return a list of subjects associated with the given program
     */
    List<Subject> findByProgramId(String programId);
}
