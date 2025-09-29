package edu.dosw.sirha.model.entities;

public enum PetitionPriority {
    URGENT("Urgente"),
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja");

    private final String description;

    PetitionPriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
