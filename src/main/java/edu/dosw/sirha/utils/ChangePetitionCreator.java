package edu.dosw.sirha.utils;

public class ChangePetitionCreator extends ClassPetitionCreator {

    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        String studentId, String... groupIds) {
        if (groupIds.length < 2) {
            throw new IllegalArgumentException("ChangePetition requiere currentGroupId y targetGroupId");
        }
        return new ChangePetition(subjectCode, observations, studentId, groupIds[0], groupIds[1]);
    }
}
