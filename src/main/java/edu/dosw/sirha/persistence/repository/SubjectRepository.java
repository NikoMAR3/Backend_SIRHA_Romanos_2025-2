package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends MongoRepository<Subject, String> {
    /**
     * Find subject by code
     */
    Optional<Subject> findByCode(String code);

    /**
     * Find subjects by name containing
     */
    List<Subject> findByNameContainingIgnoreCase(String name);

    /**
     * Find subjects by credits
     */
    List<Subject> findByCredits(int credits);

    /**
     * Find subjects by level
     */
    List<Subject> findByLevel(int level);

    /**
     * Find subjects that have specific prerequisite
     */
    List<Subject> findByPreRequisitesIdsContaining(String prerequisiteId);

    /**
     * Find subjects that have class sessions
     */
    List<Subject> findByClassSessionsIdsContaining(String classSessionId);

    /**
     * Find subjects by credits range
     */
    List<Subject> findByCreditsBetween(int minCredits, int maxCredits);

    /**
     * Find subjects by level range
     */
    List<Subject> findByLevelBetween(int minLevel, int maxLevel);

    /**
     * Find subjects without prerequisites
     */
    @Query("{'$or': [{'preRequisitesIds': {$exists: false}}, {'preRequisitesIds': {$size: 0}}]}")
    List<Subject> findSubjectsWithoutPrerequisites();

    /**
     * Find subjects ordered by level
     */
    List<Subject> findAllByOrderByLevelAsc();

    /**
     * Find subjects by multiple codes
     */
    List<Subject> findByCodeIn(List<String> codes);
}