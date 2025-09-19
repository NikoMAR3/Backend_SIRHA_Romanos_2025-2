package edu.dosw.sirha.model;

import  edu.dosw.sirha.services.PetitionsLoader;
import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.model.ChangePetition;
import edu.dosw.sirha.model.Dean;
import edu.dosw.sirha.model.Student;
import edu.dosw.sirha.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeanIntegrationTest {

    private PetitionAssistant assistant;
    private PetitionManager petitionManager;
    private PetitionsLoader loader;
    private Dean dean;
    private Student student;

    @BeforeEach
    void setUp() throws Exception {
        assistant = new PetitionAssistant();
        petitionManager = mock(PetitionManager.class);
        loader = new PetitionsLoader();
        loader.setAssistant(assistant);
        loader.setPetitionManager(petitionManager);

        dean = new Dean("DEAN001", "Computer Science");
        java.lang.reflect.Field assistantField = Dean.class.getDeclaredField("assistant");
        assistantField.setAccessible(true);
        assistantField.set(dean, assistant);

        student = new Student("Juan Perez", "stu-001");
        student.addObserver(loader);
    }

    @Test
    void studentCreatesPetition_LoadedIntoAssistant() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student.getId(),
                "CS101-01", "CS101-02"
        );

        // Student crea petición -> notifica al loader -> loader la pasa al assistant
        student.addPetition(petition);

        PetitionCommand command = assistant.getCommandByPetition(petition);

        assertNotNull(command, "El command debe estar cargado en el assistant");
        assertEquals(petition, command.getPetitionOfCommand(), "El command debe envolver la misma petición");
    }

    @Test
    void deanApprovesPetition_CommandExecutedAndApproved() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student.getId(),
                "CS101-01", "CS101-02"
        );
        student.addPetition(petition);

        PetitionCommand command = spy(assistant.getCommandByPetition(petition));
        // remplazamos el command real por un spy

        dean.answerPetition(assistant.getCommandByPetition(petition).getPetitionOfCommand(), true);

        verify(command).execute();
        assertEquals("APPROVED", petition.getStatus());
    }

    @Test
    void deanRejectsPetition_CommandUndoneAndRejected() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student.getId(),
                "CS101-01", "CS101-02"
        );
        student.addPetition(petition);

        PetitionCommand command = spy(assistant.getCommandByPetition(petition));
        assistant.addCommand(petition.getType(), command);

        dean.answerPetition(assistant.getCommandByPetition(petition).getPetitionOfCommand(), false);

        verify(command).undo();
        assertEquals("REJECTED", petition.getStatus());
    }
}
