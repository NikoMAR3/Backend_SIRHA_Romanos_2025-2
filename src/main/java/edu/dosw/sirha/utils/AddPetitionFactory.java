package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.AddPetition;
import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.Student;

public class AddPetitionCreator extends ClassPetitionCreator {

    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        Student student, String... groupIds) {
        if (groupIds.length < 1) {
            throw new IllegalArgumentException("AddPetition requiere targetGroupId");
        }
        return new AddPetition(subjectCode, observations, student, groupIds[0]);
    }
}
