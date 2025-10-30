package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Student;
import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.entities.AcademicStatus;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.persistence.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ClassSessionService classSessionService;
    @Mock
    private TrafficLightService trafficLightService;
    @Mock
    private ScheduleService scheduleService;
    @InjectMocks
    private StudentService studentService;
    private Student student;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDTO = new UserDTO();
        userDTO.setName("John Doe");
        userDTO.setMail("john@example.com");
        userDTO.setDocument("12345");
        student = new Student("1", "John Doe", "john@example.com", "12345");
        student.setAcademicStatus(AcademicStatus.ACTIVE);
        AcademicProgram program = new AcademicProgram();
        program.setName("Engineering");
        student.setAcademicProgram(program);
    }

    @Test
    void createStudent_success() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentService.createStudent(userDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void modifyStudent_success() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Optional<Student> result = studentService.modifyStudent("1", userDTO);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(studentRepository).save(student);
    }

    @Test
    void modifyStudent_notFound() {
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        Optional<Student> result = studentService.modifyStudent("1", userDTO);

        assertTrue(result.isEmpty());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void deleteStudent_success() {
        when(studentRepository.existsById("1")).thenReturn(true);

        boolean deleted = studentService.deleteStudent("1");

        assertTrue(deleted);
        verify(studentRepository).deleteById("1");
    }

    @Test
    void deleteStudent_notFound() {
        when(studentRepository.existsById("1")).thenReturn(false);

        boolean deleted = studentService.deleteStudent("1");

        assertFalse(deleted);
        verify(studentRepository, never()).deleteById("1");
    }

    @Test
    void searchStudentById_success() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Student result = studentService.searchStudentById("1");

        assertEquals("John Doe", result.getName());
    }

    @Test
    void searchStudentById_notFound() {
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.searchStudentById("1"));
    }

    @Test
    void updateAcademicStatus_success() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.updateAcademicStatus("1", AcademicStatus.INACTIVE);

        assertEquals(AcademicStatus.INACTIVE, result.getAcademicStatus());
        verify(studentRepository).save(student);
    }

    @Test
    void searchAllStudents_success() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> result = studentService.searchAllStudents();

        assertEquals(1, result.size());
    }

    @Test
    void searchStudentsByName_success() {
        when(studentRepository.findByNameContainingIgnoreCase("john")).thenReturn(List.of(student));

        List<Student> result = studentService.searchStudentsByName("john");

        assertEquals(1, result.size());
    }

    @Test
    void searchStudentsByStatus_success() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> result = studentService.searchStudentsByStatus(AcademicStatus.ACTIVE);

        assertEquals(1, result.size());
    }

    @Test
    void searchStudentsByProgram_success() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> result = studentService.searchStudentsByProgram("engineering");

        assertEquals(1, result.size());
    }

    @Test
    void enrollInCourse_success() {
        studentService.enrollInCourse("1", "C1");

        verify(classSessionService).enrollStudent("1", "C1");
    }

    @Test
    void withdrawFromCourse_success() {
        studentService.withdrawFromCourse("1", "C1");

        verify(classSessionService).withdrawStudent("1", "C1");
    }

    @Test
    void getStudentSchedule_success() {
        Schedule schedule = new Schedule();
        when(scheduleService.searchScheduleByStudentId("1")).thenReturn(schedule);

        Schedule result = studentService.getStudentSchedule("1");

        assertNotNull(result);
    }
}

