package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    /**
     * Find schedule by academic period ID
     */
    List<Schedule> findByAcademicPeriodId(String academicPeriodId);

    /**
     * Find schedules that contain a specific class session
     */
    List<Schedule> findByClassesIdsContaining(String classSessionId);

    /**
     * Find schedule by academic period (single result expected)
     */
    Optional<Schedule> findFirstByAcademicPeriodId(String academicPeriodId);
}