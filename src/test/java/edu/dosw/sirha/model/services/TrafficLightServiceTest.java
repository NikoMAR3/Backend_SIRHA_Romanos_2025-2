package edu.dosw.sirha.model.services;


import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.TrafficLightStatus;
import edu.dosw.sirha.model.persistence.repository.TrafficLightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrafficLightServiceTest {
    @Mock
    private TrafficLightRepository trafficLightRepository;
    @InjectMocks
    private TrafficLightService trafficLightService;
    private TrafficLight trafficLight;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trafficLight = new TrafficLight();
        trafficLight.setId("1");
        trafficLight.setStudentId("student1");
        trafficLight.setProgramId("program1");
        trafficLight.setGrade(3.5);
        trafficLight.setFailedSubjects(List.of());
        trafficLight.setOnGoingSubjects(List.of());
        Subject subject = new Subject();
        subject.setId(String.valueOf(1));
        subject.setName("Math");
        trafficLight.setApprovedSubjects(List.of(subject));
    }

    @Test
    void createTrafficLight_success() {
        when(trafficLightRepository.save(any(TrafficLight.class))).thenReturn(trafficLight);

        TrafficLight result = trafficLightService.createTrafficLight(trafficLight);

        assertNotNull(result);
        assertEquals(TrafficLightStatus.GREEN, result.getStatus());
        verify(trafficLightRepository).save(trafficLight);
    }

    @Test
    void deleteTrafficLight_success() {
        when(trafficLightRepository.existsById("1")).thenReturn(true);

        boolean deleted = trafficLightService.deleteTrafficLight("1");

        assertTrue(deleted);
        verify(trafficLightRepository).deleteById("1");
    }

    @Test
    void deleteTrafficLight_notFound() {
        when(trafficLightRepository.existsById("1")).thenReturn(false);

        boolean deleted = trafficLightService.deleteTrafficLight("1");

        assertFalse(deleted);
        verify(trafficLightRepository, never()).deleteById(anyString());
    }

    @Test
    void updateTrafficLight_success() {
        when(trafficLightRepository.save(any(TrafficLight.class))).thenReturn(trafficLight);

        TrafficLight result = trafficLightService.updateTrafficLight(trafficLight);

        assertEquals(TrafficLightStatus.GREEN, result.getStatus());
        verify(trafficLightRepository).save(trafficLight);
    }

    @Test
    void searchTrafficLightByStudentId_success() {
        when(trafficLightRepository.findByStudentId("student1")).thenReturn(Optional.of(trafficLight));

        Optional<TrafficLight> result = trafficLightService.searchTrafficLightByStudentId("student1");

        assertTrue(result.isPresent());
        assertEquals("student1", result.get().getStudentId());
    }

    @Test
    void searchAllTrafficLights_success() {
        when(trafficLightRepository.findAll()).thenReturn(List.of(trafficLight));

        List<TrafficLight> result = trafficLightService.searchAllTrafficLights();

        assertEquals(1, result.size());
    }

    @Test
    void searchTrafficLightByProgram_success() {
        when(trafficLightRepository.findByProgramId("program1")).thenReturn(List.of(trafficLight));

        List<TrafficLight> result = trafficLightService.searchTrafficLightByProgram("program1");

        assertEquals(1, result.size());
    }

    @Test
    void getTrafficLightHistory_success() {
        when(trafficLightRepository.findAllByStudentId("student1")).thenReturn(List.of(trafficLight));

        List<TrafficLight> result = trafficLightService.getTrafficLightHistory("student1");

        assertEquals(1, result.size());
    }

    @Test
    void getStudentsAtRisk_success() {
        trafficLight.setStatus(TrafficLightStatus.RED);
        when(trafficLightRepository.findByStatus(TrafficLightStatus.RED)).thenReturn(List.of(trafficLight));

        List<TrafficLight> result = trafficLightService.getStudentsAtRisk();

        assertEquals(1, result.size());
        assertEquals(TrafficLightStatus.RED, result.get(0).getStatus());
    }

    @Test
    void calculateTrafficLightStatus_redDueToGrade() {
        trafficLight.setGrade(2.5);

        TrafficLightStatus status = trafficLightService.calculateTrafficLightStatus(trafficLight);

        assertEquals(TrafficLightStatus.RED, status);
    }

    //@Test
    //void calculateTrafficLightStatus_redDueToFailedSubjects() {
        //trafficLight.setFailedSubjects(List.of("Physics"));

        //TrafficLightStatus status = trafficLightService.calculateTrafficLightStatus(trafficLight);

        //assertEquals(TrafficLightStatus.RED, status);
    //}

    //@Test
    //void calculateTrafficLightStatus_green() {
        //trafficLight.setApprovedSubjects(List.of("Math"));
        //trafficLight.setGrade(4.0);

        //TrafficLightStatus status = trafficLightService.calculateTrafficLightStatus(trafficLight);

        //assertEquals(TrafficLightStatus.GREEN, status);
    //}

    @Test
    void calculateTrafficLightStatus_blue() {
        trafficLight.setApprovedSubjects(List.of());
        trafficLight.setFailedSubjects(List.of());
        trafficLight.setGrade(3.5);

        TrafficLightStatus status = trafficLightService.calculateTrafficLightStatus(trafficLight);

        assertEquals(TrafficLightStatus.BLUE, status);
    }

    @Test
    void getTrafficLightStadistics_success() {
        trafficLight.setStatus(TrafficLightStatus.GREEN);
        when(trafficLightRepository.findAll()).thenReturn(List.of(trafficLight));

        Map<String, Long> stats = trafficLightService.getTrafficLightStadistics();

        assertEquals(1L, stats.get("GREEN"));
        assertEquals(1L, stats.get("TOTAL"));
    }

    @Test
    void getTrafficLightStadisticsByProgram_success() {
        trafficLight.setStatus(TrafficLightStatus.RED);
        when(trafficLightRepository.findByProgramId("program1")).thenReturn(List.of(trafficLight));

        Map<String, Long> stats = trafficLightService.getTrafficLightStadisticsByProgram("program1");

        assertEquals(1L, stats.get("RED"));
        assertEquals(1L, stats.get("TOTAL"));
    }
}