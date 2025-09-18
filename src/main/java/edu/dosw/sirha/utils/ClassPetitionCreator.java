package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassPetition;

public abstract class ClassPetitionCreator {

    public abstract ClassPetition createPetition(String subjectCode, String observations,
                                                 String studentId, String... groupIds);

    public static ClassPetitionCreator getCreator(String type) {
        switch (type.toUpperCase()) {
            case "ADD":
                return new AddPetitionCreator();
            case "REMOVE":
                return new RemovePetitionCreator();
            case "CHANGE":
                return new ChangePetitionCreator();
            default:
                throw new IllegalArgumentException("Tipo de petición no válido: " + type);
        }
    }
}
