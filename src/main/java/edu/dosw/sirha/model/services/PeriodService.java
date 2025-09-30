package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Period;
import edu.dosw.sirha.model.persistence.repository.PeriodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeriodService {

    private final PeriodRepository periodRepository;

    public PeriodService(PeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    /**
     * Creates a new academic period in the database.
     * @param period the Period entity to create
     * @return the saved Period entity
     */
    public Period createPeriod(Period period) {
        return periodRepository.save(period);
    }

    /**
     * Modifies an existing academic period.
     * @param period the Period entity with updated data
     * @return the modified Period entity
     */
    public Period modifyPeriod(Period period) {
        return periodRepository.save(period);
    }

    /**
     * Deletes an academic period by its ID.
     * @param id the ID of the period to delete
     */
    public void deletePeriod(String id) {
        periodRepository.deleteById(id);
    }

    /**
     * Gets all enabled periods.
     * @return list of enabled periods
     */
    public List<Period> checkEnabledPeriods() {
        return periodRepository.findEnabledPeriods();
    }
}
