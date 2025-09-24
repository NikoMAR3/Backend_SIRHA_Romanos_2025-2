package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.Student;

public class ChangePetitionFactory extends ClassPetitionFactory {

    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        Student student, String... groupIds) {
        if (groupIds.length < 2) {
            throw new IllegalArgumentException("ChangePetition requiere currentGroupId y targetGroupId");
        }
        return new ChangePetition(subjectCode, observations, student, groupIds[0], groupIds[1]);
    }
}
