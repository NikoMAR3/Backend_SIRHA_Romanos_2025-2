package edu.dosw.sirha.services;

import edu.dosw.sirha.controller.dto.StudentRequestDTO;
import edu.dosw.sirha.controller.dto.StudentResponseDTO;
import edu.dosw.sirha.controller.dto.StudentScheduleResponseDTO;
import edu.dosw.sirha.controller.dto.TrafficLightResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class StudentService {
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO studentRequest) {
        return null;
    }

    public Page<StudentResponseDTO> getAllStudents(Pageable pageable) {
        return null;
    }

    public StudentResponseDTO getStudentById(Long id) {
        return null;
    }

    public StudentResponseDTO createStudent(StudentRequestDTO studentRequest) {
        return null;
    }

    public StudentScheduleResponseDTO getStudentSchedule(Long id) {
        return null;
    }

    public TrafficLightResponseDTO getStudentTrafficLight(Long id) {
        return null;
    }
}
