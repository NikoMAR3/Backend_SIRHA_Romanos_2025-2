package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.TrafficLightStatus;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import edu.dosw.sirha.model.persistence.repository.TrafficLightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrafficLightServiceTest {

    private TrafficLightRepository trafficLightRepository;
    private SubjectRepository subjectRepository;
    private TrafficLightService service;

    @BeforeEach
    void setUp() {
        trafficLightRepository = mock(TrafficLightRepository.class);
        subjectRepository = mock(SubjectRepository.class);
        service = new TrafficLightService(trafficLightRepository, subjectRepository);
    }

    @Test
    void createTrafficLight_setsStatusAndSaves() {
        TrafficLight tl = new TrafficLight();
        tl.setFailedSubjects(new HashMap<>());
        tl.setApprovedSubjects(new HashMap<>());
        tl.setOnGoingSubjects(new ArrayList<>());
        tl.setGrade(3.5);
        when(trafficLightRepository.save(any())).thenReturn(tl);

        TrafficLight result = service.createTrafficLight(tl);

        assertNotNull(result);
        assertEquals(TrafficLightStatus.BLUE, tl.getStatus());
        verify(trafficLightRepository).save(tl);
    }

    @Test
    void deleteTrafficLight_exists_deletes() {
        String id = "123";
        when(trafficLightRepository.existsById(id)).thenReturn(true);

        assertTrue(service.deleteTrafficLight(id));
        verify(trafficLightRepository).deleteById(id);
    }

    @Test
    void deleteTrafficLight_notExists_returnsFalse() {
        String id = "123";
        when(trafficLightRepository.existsById(id)).thenReturn(false);

        assertFalse(service.deleteTrafficLight(id));
        verify(trafficLightRepository, never()).deleteById(any());
    }

    @Test
    void updateTrafficLight_setsStatusAndSaves() {
        TrafficLight tl = new TrafficLight();
        tl.setFailedSubjects(new HashMap<>());
        HashMap<String, Integer> approved = new HashMap<>();
        approved.put("Math", 4);
        tl.setApprovedSubjects(approved);
        tl.setOnGoingSubjects(new ArrayList<>());
        tl.setGrade(4.0);
        when(trafficLightRepository.save(any())).thenReturn(tl);

        TrafficLight result = service.updateTrafficLight(tl);

        assertNotNull(result);
        assertEquals(TrafficLightStatus.GREEN, tl.getStatus()); // Ahora sí será "Aprobado"
        verify(trafficLightRepository).save(tl);
    }

    @Test
    void searchTrafficLightByStudentId_found() {
        TrafficLight tl = new TrafficLight();
        when(trafficLightRepository.findByStudentId("stu1")).thenReturn(Optional.of(tl));

        Optional<TrafficLight> result = service.searchTrafficLightByStudentId("stu1");
        assertTrue(result.isPresent());
        assertEquals(tl, result.get());
    }

    @Test
    void searchAllTrafficLights_returnsList() {
        List<TrafficLight> list = List.of(new TrafficLight());
        when(trafficLightRepository.findAll()).thenReturn(list);

        assertEquals(list, service.searchAllTrafficLights());
    }

    @Test
    void searchTrafficLightByProgram_returnsList() {
        List<TrafficLight> list = List.of(new TrafficLight());
        when(trafficLightRepository.findByProgramId("prog1")).thenReturn(list);

        assertEquals(list, service.searchTrafficLightByProgram("prog1"));
    }

    @Test
    void calculateTrafficLightStatus_RED_byFailedSubjects() {
        TrafficLight tl = new TrafficLight();
        HashMap<String, Integer> failed = new HashMap<>();
        failed.put("Math", 2);
        tl.setFailedSubjects(failed);
        tl.setApprovedSubjects(new HashMap<>());
        tl.setOnGoingSubjects(new ArrayList<>());
        tl.setGrade(3.5);

        assertEquals(TrafficLightStatus.RED, service.calculateTrafficLightStatus(tl));
    }

    @Test
    void calculateTrafficLightStatus_RED_byLowGrade() {
        TrafficLight tl = new TrafficLight();
        tl.setFailedSubjects(new HashMap<>());
        tl.setApprovedSubjects(new HashMap<>());
        tl.setOnGoingSubjects(new ArrayList<>());
        tl.setGrade(2.5);

        assertEquals(TrafficLightStatus.RED, service.calculateTrafficLightStatus(tl));
    }

    @Test
    void calculateTrafficLightStatus_GREEN() {
        TrafficLight tl = new TrafficLight();
        HashMap<String, Integer> approved = new HashMap<>();
        approved.put("Math", 4);
        tl.setFailedSubjects(new HashMap<>());
        tl.setApprovedSubjects(approved);
        tl.setOnGoingSubjects(new ArrayList<>());
        tl.setGrade(3.0);

        assertEquals(TrafficLightStatus.GREEN, service.calculateTrafficLightStatus(tl));
    }

    @Test
    void calculateTrafficLightStatus_BLUE() {
        TrafficLight tl = new TrafficLight();
        tl.setFailedSubjects(new HashMap<>());
        tl.setApprovedSubjects(new HashMap<>());
        tl.setOnGoingSubjects(new ArrayList<>());
        tl.setGrade(3.5);

        assertEquals(TrafficLightStatus.BLUE, service.calculateTrafficLightStatus(tl));
    }

    @Test
    void getTrafficLightStadistics_countsStatuses() {
        TrafficLight green = new TrafficLight(); green.setStatus(TrafficLightStatus.GREEN);
        TrafficLight blue = new TrafficLight(); blue.setStatus(TrafficLightStatus.BLUE);
        TrafficLight red = new TrafficLight(); red.setStatus(TrafficLightStatus.RED);
        TrafficLight white = new TrafficLight(); white.setStatus(TrafficLightStatus.WHITE);
        List<TrafficLight> all = List.of(green, blue, red, white, green);

        when(trafficLightRepository.findAll()).thenReturn(all);

        Map<String, Long> stats = service.getTrafficLightStadistics();
        assertEquals(2, stats.get("GREEN"));
        assertEquals(1, stats.get("BLUE"));
        assertEquals(1, stats.get("WHITE"));
        assertEquals(1, stats.get("RED"));
        assertEquals(5, stats.get("TOTAL"));
    }

    @Test
    void getTrafficLightStadisticsByProgram_countsStatuses() {
        TrafficLight green = new TrafficLight(); green.setStatus(TrafficLightStatus.GREEN);
        TrafficLight blue = new TrafficLight(); blue.setStatus(TrafficLightStatus.BLUE);
        List<TrafficLight> all = List.of(green, blue);

        when(trafficLightRepository.findByProgramId("p1")).thenReturn(all);

        Map<String, Long> stats = service.getTrafficLightStadisticsByProgram("p1");
        assertEquals(1, stats.get("GREEN"));
        assertEquals(1, stats.get("BLUE"));
        assertEquals(0, stats.get("WHITE"));
        assertEquals(0, stats.get("RED"));
        assertEquals(2, stats.get("TOTAL"));
    }

    @Test
    void getTrafficLightHistory_returnsList() {
        List<TrafficLight> list = List.of(new TrafficLight());
        when(trafficLightRepository.findAllByStudentId("stu1")).thenReturn(list);

        assertEquals(list, service.getTrafficLightHistory("stu1"));
    }

    @Test
    void getStudentsAtRisk_returnsRed() {
        List<TrafficLight> list = List.of(new TrafficLight());
        when(trafficLightRepository.findByStatus(TrafficLightStatus.RED)).thenReturn(list);

        assertEquals(list, service.getStudentsAtRisk());
    }

    @Test
    void calculateGPA_returnsCorrectValue() {
        TrafficLight tl = new TrafficLight();
        HashMap<String, Integer> approved = new HashMap<>();
        approved.put("Math", 4);
        tl.setApprovedSubjects(approved);
        tl.setFailedSubjects(new HashMap<>());
        when(trafficLightRepository.findByStudentId("stu1")).thenReturn(Optional.of(tl));

        Subject subject = mock(Subject.class);
        when(subject.getCredits()).thenReturn(3);
        when(subjectRepository.findByName("Math")).thenReturn(Optional.of(subject));
        // Simulate grade value as 4.0
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);

        HashMap<Subject, Double> grades = new HashMap<>();
        grades.put(subject, 4.0);

        // test getSubjectsWithGrades
        HashMap<Subject, Double> result = service.getSubjectsWithGrades("stu1");
        assertTrue(result.containsKey(subject));

        // test calculateGPA
        double gpa = service.calculateGPA("stu1");
        assertEquals(4.0, gpa, 0.001);
    }

    @Test
    void getSubjectsWithGrades_emptyIfNotFound() {
        when(trafficLightRepository.findByStudentId("unknown")).thenReturn(Optional.empty());
        HashMap<Subject, Double> result = service.getSubjectsWithGrades("unknown");
        assertTrue(result.isEmpty());
    }
}