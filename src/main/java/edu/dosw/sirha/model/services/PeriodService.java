package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Period;
import edu.dosw.sirha.model.persistence.repository.PeriodRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
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
     * @throws DataAccessException if database operation fails
     */
    public Period createPeriod(Period period) {
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }

        validatePeriodDates(period);

        if (period.getEnabled() == null) {
            period.setEnabled(true);
        }

        try {
            return periodRepository.save(period);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to create period: " + e.getMessage(), e) {};
        }
    }

    /**
     * Modifies an existing academic period.
     * @param period the Period entity with updated data
     * @return the modified Period entity
     * @throws IllegalArgumentException if period is null, has invalid data, or doesn't exist
     * @throws DataAccessException if database operation fails
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

        try {
            return periodRepository.save(period);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to modify period: " + e.getMessage(), e) {};
        }
    }

    /**
     * Deletes an academic period by its ID.
     * @param id the ID of the period to delete
     * @throws IllegalArgumentException if ID is null or empty, or period doesn't exist
     * @throws DataAccessException if database operation fails
     */
    public void deletePeriod(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        if (!periodRepository.existsById(id)) {
            throw new IllegalArgumentException("Period with ID '" + id + "' does not exist");
        }

        try {
            periodRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Period with ID '" + id + "' does not exist", e);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to delete period: " + e.getMessage(), e) {};
        }
    }

    /**
     * Searches for a period by its ID.
     * @param id the ID of the period
     * @return the found Period entity
     * @throws IllegalArgumentException if ID is null or empty, or period doesn't exist
     * @throws DataAccessException if database operation fails
     */
    public Period searchPeriodById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        try {
            Optional<Period> period = periodRepository.findById(id);
            return period.orElseThrow(() ->
                    new IllegalArgumentException("Period with ID '" + id + "' not found"));
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to search period by ID: " + e.getMessage(), e) {};
        }
    }

    /**
     * Gets all enabled periods.
     * @return list of enabled periods
     * @throws DataAccessException if database operation fails
     */
    public List<Period> checkEnabledPeriods() {
        try {
            return periodRepository.findEnabledPeriods();
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to retrieve enabled periods: " + e.getMessage(), e) {};
        }
    }

    /**
     * Gets all periods.
     * @return list of all periods
     * @throws DataAccessException if database operation fails
     */
    public List<Period> searchAllPeriods() {
        try {
            return periodRepository.findAll();
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to retrieve all periods: " + e.getMessage(), e) {};
        }
    }

    /**
     * Enables or disables a period.
     * @param id the ID of the period
     * @param enabled true to enable, false to disable
     * @throws IllegalArgumentException if ID is null or empty, or period doesn't exist
     * @throws DataAccessException if database operation fails
     */
    public void togglePeriodStatus(String id, boolean enabled) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        try {
            Period period = searchPeriodById(id);
            period.setEnabled(enabled);
            periodRepository.save(period);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to toggle period status: " + e.getMessage(), e) {};
        }
    }

    /**
     * Checks if a period exists by its ID.
     * @param id the ID of the period
     * @return true if the period exists, false otherwise
     * @throws IllegalArgumentException if ID is null or empty
     * @throws DataAccessException if database operation fails
     */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be null or empty");
        }

        try {
            return periodRepository.existsById(id);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to check period existence: " + e.getMessage(), e) {};
        }
    }

    /**
     * Validates period dates.
     * @param period the period to validate
     * @throws IllegalArgumentException if dates are invalid
     */
    private void validatePeriodDates(Period period) {
        if (period.getStartDate() == null || period.getStartDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        if (period.getEndDate() == null || period.getEndDate().trim().isEmpty()) {
            throw new IllegalArgumentException("End date cannot be null or empty");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(period.getStartDate(), formatter);
            LocalDate endDate = LocalDate.parse(period.getEndDate(), formatter);

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd", e);
        }
    }
}
