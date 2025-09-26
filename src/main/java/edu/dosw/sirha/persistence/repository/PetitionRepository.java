package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.Petition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PetitionRepository extends MongoRepository<Petition, String> {
    /**
     * Find petitions by student ID
     */
    List<Petition> findByStudentId(String studentId);

    /**
     * Find petitions by type
     */
    List<Petition> findByType(String type);

    /**
     * Find petitions by status
     */
    List<Petition> findByStatus(String status);

    /**
     * Find petitions by subject code
     */
    List<Petition> findBySubjectCode(String subjectCode);

    /**
     * Find petitions by type and status
     */
    List<Petition> findByTypeAndStatus(String type, String status);

    /**
     * Find petitions by student ID and status
     */
    List<Petition> findByStudentIdAndStatus(String studentId, String status);

    /**
     * Find pending petitions ordered by creation date
     */
    List<Petition> findByStatusOrderByDateOfCreationAsc(String status);

    /**
     * Find petitions created between dates
     */
    List<Petition> findByDateOfCreationBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find petitions by target group ID
     */
    List<Petition> findByTargetGroupId(String targetGroupId);

    /**
     * Find petitions by current group ID
     */
    List<Petition> findByCurrentGroupId(String currentGroupId);

    /**
     * Count petitions by status
     */
    long countByStatus(String status);

    /**
     * Count petitions by type
     */
    long countByType(String type);

    /**
     * Find petitions that need processing (pendientes más antiguas)
     */
    @Query("{'status': 'PENDIENTE'}")
    List<Petition> findPendingPetitionsOrderByPriority();
}
