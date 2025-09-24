package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.Student;

public abstract class ClassPetitionFactory {

    public abstract ClassPetition createPetition(String subjectCode, String observations,
                                                 Student student, String... groupIds);

    public static ClassPetitionFactory getCreator(String type) {
        switch (type.toUpperCase()) {
            case "ADD":
                return new AddPetitionFactory();
            case "REMOVE":
                return new RemovePetitionFactory();
            case "CHANGE":
                return new ChangePetitionFactory();
            default:
                throw new IllegalArgumentException("Tipo de petición no válido: " + type);
        }
    }
}
