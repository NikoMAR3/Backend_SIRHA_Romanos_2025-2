package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Period;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Repository interface for managing Period entities.
 *
 * Provides CRUD operations through MongoRepository and
 * a custom query to find enabled periods.
 */
public interface PeriodRepository extends MongoRepository<Period, String> {

    /**
     * Finds all periods that are enabled.
     *
     * @return list of enabled periods
     */
    @Query("{ 'enabled': true }")
    List<Period> findEnabledPeriods();

}
