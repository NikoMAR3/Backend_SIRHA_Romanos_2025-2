package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Period;
import edu.dosw.sirha.model.persistence.repository.PeriodRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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
     * @throws IllegalArgumentException if period is null or has invalid data
     */
    public Period createPeriod(Period period) {
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }

        validatePeriodDates(period);

        if (period.getEnabled() == null) {
            period.setEnabled(true);
        }

        return periodRepository.save(period);
    }

    /**
     * Modifies an existing academic period.
     * @param period the Period entity with updated data
     * @return the modified Period entity
     * @throws IllegalArgumentException if period is null, has invalid data, or doesn't exist
     */
    public Period modifyPeriod(Period period) {
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }
        if (period.getId() == null || period.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty for modification");
        }

        validatePeriodDates(period);

        if (!periodRepository.existsById(period.getId())) {
            throw new IllegalArgumentException("Period with ID '" + period.getId() + "' does not exist");
        }

        return periodRepository.save(period);
    }

    /**
     * Deletes an academic period by its ID.
     * @param id the ID of the period to delete
     * @throws IllegalArgumentException if ID is null or empty, or period doesn't exist
     */
    public void deletePeriod(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        if (!periodRepository.existsById(id)) {
            throw new IllegalArgumentException("Period with ID '" + id + "' does not exist");
        }

        periodRepository.deleteById(id);
    }

    /**
     * Searches for a period by its ID.
     * @param id the ID of the period
     * @return the found Period entity
     * @throws IllegalArgumentException if ID is null or empty, or period doesn't exist
     */
    public Period searchPeriodById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        Optional<Period> period = periodRepository.findById(id);
        return period.orElseThrow(() ->
                new IllegalArgumentException("Period with ID '" + id + "' not found"));
    }

    /**
     * Gets all enabled periods.
     * @return list of enabled periods
     */
    public List<Period> checkEnabledPeriods() {
        return periodRepository.findEnabledPeriods();
    }

    /**
     * Gets all periods.
     * @return list of all periods
     */
    public List<Period> searchAllPeriods() {
        return periodRepository.findAll();
    }

    /**
     * Enables or disables a period.
     * @param id the ID of the period
     * @param enabled true to enable, false to disable
     * @throws IllegalArgumentException if ID is null or empty, or period doesn't exist
     */
    public void togglePeriodStatus(String id, boolean enabled) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        Period period = searchPeriodById(id);
        period.setEnabled(enabled);
        periodRepository.save(period);
    }

    /**
     * Checks if a period exists by its ID.
     * @param id the ID of the period
     * @return true if the period exists, false otherwise
     * @throws IllegalArgumentException if ID is null or empty
     */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        return periodRepository.existsById(id);
    }

    /**
     * Validates period dates.
     * @param period the period to validate
     * @throws IllegalArgumentException if dates are invalid
     * @throws DateTimeParseException if date format is invalid
     */
    private void validatePeriodDates(Period period) {
        if (period.getStartDate() == null || period.getStartDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        if (period.getEndDate() == null || period.getEndDate().trim().isEmpty()) {
            throw new IllegalArgumentException("End date cannot be null or empty");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(period.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(period.getEndDate(), formatter);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
