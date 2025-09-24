package edu.dosw.sirha.model;

import edu.dosw.sirha.services.PetitionsLoader;
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
    private ScheduleManager scheduleManager;
    private TrafficLightManager trafficLightManager;
    private ClassManager classManager;
    private PetitionsLoader loader;
    private Dean dean;
    private Student student;

    @BeforeEach
    void setUp() throws Exception {

        scheduleManager = mock(ScheduleManager.class);
        petitionManager = mock(PetitionManager.class);
        trafficLightManager = mock(TrafficLightManager.class);
        classManager = mock(ClassManager.class);


        assistant = new PetitionAssistant();


        loader = new PetitionsLoader();
        loader.setAssistant(assistant);
        loader.setPetitionManager(petitionManager);

        dean = new Dean(scheduleManager, petitionManager, trafficLightManager,
                classManager, assistant);

        dean.configure("DEAN001", "Computer Science");

        student = new Student("Juan Perez", "stu-001");
        student.addObserver(loader);
    }

    @Test
    void studentCreatesPetition_LoadedIntoAssistant() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student,
                "CS101-01", "CS101-02"
        );

        student.addPetition(petition);

        PetitionCommand command = assistant.getCommandByPetition(petition);

        assertNotNull(command, "El command debe estar cargado en el assistant");
        assertEquals(petition, command.getPetition(), "El command debe envolver la misma petición");
    }

    @Test
    void deanApprovesPetition_CommandExecutedAndApproved() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student,
                "CS101-01", "CS101-02"
        );
        student.addPetition(petition);

        PetitionCommand originalCommand = assistant.getCommandByPetition(petition);
        PetitionCommand spyCommand = spy(originalCommand);

        assistant.addCommand(petition.getType(), spyCommand);

        dean.answerPetition(petition, true);

        verify(spyCommand).execute();
        assertEquals("APPROVED", petition.getStatus());
    }

    @Test
    void deanRejectsPetition_CommandUndoneAndRejected() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student,
                "CS101-01", "CS101-02"
        );
        student.addPetition(petition);

        PetitionCommand originalCommand = assistant.getCommandByPetition(petition);
        PetitionCommand spyCommand = spy(originalCommand);

        assistant.addCommand(petition.getType(), spyCommand);

        dean.answerPetition(petition, false);

        verify(spyCommand).undo();
        assertEquals("DENIED", petition.getStatus());
    }


    @Test
    void deanHasAllDependenciesInjected() {
        assertNotNull(dean, "Dean debe estar inicializado");
        assertEquals("DEAN001", dean.getDean_id());
        assertEquals("Computer Science", dean.getMajor());

        assertDoesNotThrow(() -> {
            dean.checkPetitions("CHANGE");
            dean.checkTrafficLight();
        });
    }
}