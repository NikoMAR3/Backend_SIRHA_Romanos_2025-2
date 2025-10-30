package edu.dosw.sirha.model.entities;

/**
 * Enum representing the academic status of a student.
 */
public enum AcademicStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido"),
    GRADUATED("Graduado");

    private final String description;

    /**
     * Constructor for AcademicStatus enum.
     * @param description The description of the academic status.
     */
    AcademicStatus(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the academic status.
     * @return The description of the academic status.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the string representation of the academic status.
     * @return The description of the academic status.
     */
    @Override
    public String toString() {
        return description;
    }
}
