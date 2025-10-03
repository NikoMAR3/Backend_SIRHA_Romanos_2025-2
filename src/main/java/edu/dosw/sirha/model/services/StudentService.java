package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Student;
import edu.dosw.sirha.model.persistence.repository.StudentRepository;
import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.entities.AcademicStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(UserDTO dto) {
        Student student = new Student(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return studentRepository.save(student);
    }

    public Optional<Student> modifyStudent(String id, UserDTO dto) {
        return studentRepository.findById(id)
                .map(student -> {
                    student.setName(dto.getName());
                    student.setMail(dto.getMail());
                    student.setDocument(dto.getDocument());
                    return studentRepository.save(student);
                });
    }


    public boolean deleteStudent(String id) {
        if(studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Student searchStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public Student updateAcademicStatus(String id, AcademicStatus status) {
        Student student = searchStudentById(id);
        student.setAcademicStatus(status);
        return studentRepository.save(student);
    }

    public List<Student> searchAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> searchStudentsByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Student> searchStudentsByStatus(AcademicStatus status) {
        return studentRepository.findAll()
                .stream()
                .filter(s -> s.getAcademicStatus() == status)
                .toList();
    }

    public List<Student> searchStudentsByProgram(String program) {
        return studentRepository.findAll() .stream() .filter(s -> s.getAcademicProgram().getName().equalsIgnoreCase(program)) .toList();
    }

    public void enrollInCourse(String studentId, String courseId) {
    }

    public void withdrawFromCourse(String studentId, String courseId) {

    }

    public double calculateGPA(String studentId) {
        return 0.0;
    }

    public Schedule getStudentSchedule(String studentId) {
        return new Schedule();
    }
}
