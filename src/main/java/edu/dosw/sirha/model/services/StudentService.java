package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Student;
import edu.dosw.sirha.model.persistence.repository.StudentRepository;
import edu.dosw.sirha.model.dto.StudentDTO;
import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.entities.enums.AcademicStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(StudentDTO dto) {
        Student student = new Student();
        student.setName(dto.getName());
        student.setMail(dto.getMail());
        student.setDocument(dto.getDocument());
        student.setStudentCode(dto.getStudentCode());
        student.setSemester(dto.getSemester());
        return studentRepository.save(student);
    }

    public boolean deleteStudent(String id) {
        if(studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Student modifyStudent(String id, StudentDTO dto) {
        Optional<Student> opt = studentRepository.findById(id);
        if(opt.isPresent()) {
            Student student = opt.get();
            student.setName(dto.getName());
            student.setMail(dto.getMail());
            student.setDocument(dto.getDocument());
            return studentRepository.save(student);
        }
        throw new RuntimeException("Student not found");
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
        return studentRepository.findAll()
                .stream()
                .filter(s -> s.getProgram().equalsIgnoreCase(program))
                .toList();
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
