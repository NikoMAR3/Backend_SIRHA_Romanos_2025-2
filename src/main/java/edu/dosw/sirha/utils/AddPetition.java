package edu.dosw.sirha.utils;

public class AddPetition extends ClassPetition {
    private String targetGroupId;

    public AddPetition() {}

    public AddPetition(String subjectCode, String observations, String studentId, String targetGroupId) {
        super("ADD", subjectCode, observations, studentId);
        this.targetGroupId = targetGroupId;
    }

    public String getTargetGroupId() { return targetGroupId; }
    public void setTargetGroupId(String targetGroupId) { this.targetGroupId = targetGroupId; }

    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando solicitud de adición a grupo: " + targetGroupId);
    }

    @Override
    public Command toCommand(PetitionManager manager) {
        return new AddStudentCommand(manager, this);
    }
}
