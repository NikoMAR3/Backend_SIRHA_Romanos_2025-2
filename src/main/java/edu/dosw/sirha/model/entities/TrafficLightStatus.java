package edu.dosw.sirha.model.entities;

import lombok.Getter;

@Getter
public enum TrafficLightStatus {
    RED("Reprobado"),
    BLUE("Matriculado"),
    WHITE("Pendiente"),
    GREEN("Aprobado");

    private final String description;

    TrafficLightStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}

