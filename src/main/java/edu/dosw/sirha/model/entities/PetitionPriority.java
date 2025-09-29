package edu.dosw.sirha.model.entities;

/**
 * Enum representing the priority levels of petitions.
 */
public enum PetitionPriority {
    URGENT("Urgente"),
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja");

    private final String description;

    /**
     * Constructor for PetitionPriority enum.
     * @param description The description of the petition priority.
     */
    PetitionPriority(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the petition priority.
     * @return The description of the petition priority.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the string representation of the petition priority.
     * @return The description of the petition priority.
     */
    @Override
    public String toString() {
        return description;
    }
}
