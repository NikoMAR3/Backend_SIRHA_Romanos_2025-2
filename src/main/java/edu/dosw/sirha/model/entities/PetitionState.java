package edu.dosw.sirha.model.entities;

/**
 * Enum representing the states of a petition.
 */
public enum PetitionState {
    PENDING("Pendiente"),
    IN_PROCESS("En revisión"),
    APPROVED("Aprobada"),
    REPROVED("Rechazada");

    private final String description;

    /**
     * Constructor for PetitionState enum.
     * @param description The description of the petition state.
     */
    PetitionState(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the petition state.
     * @return The description of the petition state.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the string representation of the petition state.
     * @return The description of the petition state.
     */
    @Override
    public String toString() {
        return description;
    }
}
