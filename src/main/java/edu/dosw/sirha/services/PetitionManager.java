
package edu.dosw.sirha.services;

import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.ClassSession;
import edu.dosw.sirha.model.Petition;
import edu.dosw.sirha.model.Student;

public class PetitionManager {

    private ClassManager classManager;

    public Boolean validateIfPossible(Petition petition) {
        switch (petition.getType()) {
            case "CHANGE":
                return validateChange((ChangePetition) petition);
            case "ADD":
                // validateAdd((AddPetition) petition);
                return true;
            case "REMOVE":
                // validateRemove((RemovePetition) petition);
                return true;
            default:
                return false;
        }
    }

    public boolean validateChange(ChangePetition petition) {
        ClassSession currentGroup = classManager.getClassById(petition.getCurrentGroupId());
        ClassSession targetGroup = classManager.getClassById(petition.getTargetGroupId());

        boolean isEnrolled = currentGroup.getStudents().stream()
                .anyMatch(s -> s.getId().equals(petition.getStudentId()));
        boolean hasQuota = targetGroup.hasAvailableQuota();



        return isEnrolled && hasQuota;
    }

    public void makeChange(ChangePetition petition) {
        if(validateChange(petition)) {
            ClassSession currentGroup = classManager.getClassById(petition.getCurrentGroupId());
            ClassSession targetGroup = classManager.getClassById(petition.getTargetGroupId());
            Student student = currentGroup.getStudents().stream()
                    .filter(s -> s.getId().equals(petition.getStudentId())) // filtra
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("El estudiante no está en el grupo actual"));;

            student.removeFromClass(currentGroup);
            student.addToClass(targetGroup);
        }
    }
}
