package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Student;
import edu.dosw.sirha.model.persistence.repository.StudentRepository;
import edu.dosw.sirha.model.entities.Schedule;
import edu.dosw.sirha.model.entities.AcademicStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing Student entities.
 * Handles comprehensive student lifecycle management including enrollment, academic status,
 * course registration, GPA calculation, and schedule management.
 *
 * Integrates with multiple services to provide complete student academic functionality:
 * - ClassSessionService for course enrollment
 * - TrafficLightService for academic performance tracking
 * - ScheduleService for timetable management
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassSessionService classSessionService;
    private final TrafficLightService trafficLightService;
    private final ScheduleService scheduleService;

    /**
     * Constructor for dependency injection of required repositories and services.
     *
     * @param studentRepository the repository for Student data access
     * @param classSessionService the service for managing class sessions and course enrollment
     * @param trafficLightService the service for academic performance evaluation and GPA calculation
     * @param scheduleService the service for managing student schedules and timetables
     */
    @Autowired
    public StudentService(StudentRepository studentRepository, ClassSessionService classSessionService,
                          TrafficLightService trafficLightService, ScheduleService scheduleService) {
        this.studentRepository = studentRepository;
        this.classSessionService = classSessionService;
        this.trafficLightService = trafficLightService;
        this.scheduleService = scheduleService;
    }

    /**
     * Creates a new Student record.
     * Generates a unique UUID for the entity and populates it with basic personal information.
     *
     * @param dto the UserDTO containing the student's basic information (name, email, document)
     * @return the newly created Student entity with assigned ID
     * @throws IllegalArgumentException if the dto is null or contains invalid data
     */
    public Student createStudent(UserDTO dto) {
        Student student = new Student(
                UUID.randomUUID().toString(),
                dto.getName(),
                dto.getMail(),
                dto.getDocument()
        );
        return studentRepository.save(student);
    }

    /**
     * Modifies an existing Student record.
     * Updates the student's personal information while preserving academic relationships and status.
     *
     * @param id the unique identifier of the Student to modify
     * @param dto the UserDTO containing the updated personal information
     * @return an Optional containing the updated Student if found, or empty Optional if not found
     */
    public Optional<Student> modifyStudent(String id, UserDTO dto) {
        return studentRepository.findById(id)
                .map(student -> {
                    student.setName(dto.getName());
                    student.setMail(dto.getMail());
                    student.setDocument(dto.getDocument());
                    return studentRepository.save(student);
                });
    }

    /**
     * Deletes a Student record by its ID.
     *
     *
     * @param id the unique identifier of the Student to delete
     * @return true if the record was successfully deleted, false if no record was found
     */
    public boolean deleteStudent(String id) {
        if(studentRepository.existsById(id)) {
            studentRepository.deleteById(id);// Debe incluir validación para garantizar que el estudiante no tenga inscripciones activas antes de la eliminación.
            return true;
        }
        return false;
    }

    /**
     * Searches for a Student by their unique identifier.
     *
     * @param id the unique identifier of the Student to find
     * @return the Student entity if found
     * @throws RuntimeException if no Student is found with the given ID
     */
    public Student searchStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    /**
     * Updates the academic status of a student.
     * Academic status can represent various states such as ACTIVE, INACTIVE,
     * GRADUATED, SUSPENDED
     *
     * @param id the unique identifier of the Student
     * @param status the new AcademicStatus to assign to the student
     * @return the updated Student entity with new academic status
     * @throws RuntimeException if no Student is found with the given ID
     */
    public Student updateAcademicStatus(String id, AcademicStatus status) {
        Student student = searchStudentById(id);
        student.setAcademicStatus(status);
        return studentRepository.save(student);
    }

    /**
     * Retrieves all Student records from the database.
     * Provides complete access to the student population for administrative purposes,
     *
     * @return a list of all Student entities in the system
     */
    public List<Student> searchAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Searches for Students by name using case-insensitive partial matching.
     *
     * @param name the name or partial name to search for
     * @return a list of Students whose names contain the search string
     */
    public List<Student> searchStudentsByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Searches for Students by their academic status.
     * Enables filtering of students based on their current academic standing
     * (e.g.,  all active students, etc.).
     *
     * @param status the AcademicStatus to filter by
     * @return a list of Students with the specified academic status
     */
    public List<Student> searchStudentsByStatus(AcademicStatus status) {
        return studentRepository.findAll()
                .stream()
                .filter(s -> s.getAcademicStatus() == status)
                .toList();
    }

    /**
     * Searches for Students enrolled in a specific academic program.
     * Supports program management by allowing retrieval of all students
     * belonging to a particular degree program or major.
     *
     * @param program the name of the academic program to filter by
     * @return a list of Students enrolled in the specified program
     */
    public List<Student> searchStudentsByProgram(String program) {
        return studentRepository.findAll()
                .stream()
                .filter(s -> s.getAcademicProgram().getName().equalsIgnoreCase(program))
                .toList();
    }

    /**
     * Enrolls a student in a specific course.
     * Delegates to ClassSessionService to handle the actual enrollment logic,
     * including validation of prerequisites and availability.
     *
     * @param studentId the unique identifier of the Student to enroll
     * @param courseId the unique identifier of the Course to enroll in
     * @throws RuntimeException if the student or course is not found, or if enrollment criteria are not met
     */
    public void enrollInCourse(String studentId, String courseId) {
        classSessionService.enrollStudent(studentId, courseId);
    }

    /**
     * Withdraws a student from a specific course.
     * Delegates to ClassSessionService to handle the withdrawal process,
     * including updating attendance records and managing waitlists if applicable.
     *
     * @param studentId the unique identifier of the Student to withdraw
     * @param courseId the unique identifier of the Course to withdraw from
     * @throws RuntimeException if the student or course is not found, or if withdrawal is not permitted
     */
    public void withdrawFromCourse(String studentId, String courseId) {
        classSessionService.withdrawStudent(studentId, courseId);
    }

    /**
     * Calculates the Grade Point Average (GPA) for a student.
     * Delegates to TrafficLightService which implements complex GPA calculation
     * logic including grade weighting, credit hours and all that stuff bruh.
     *
     * @param studentId the unique identifier of the Student
     * @return the calculated GPA as a double value
     * @throws RuntimeException if the student is not found or has no grade records
     */
    public double calculateGPA(String studentId) {return trafficLightService.calculateGPA(studentId);}
    //public double calculateGPA(String studentId) {
        //return trafficLightService.calculateGPA(studentId);
    //}

    /**
     * Retrieves the complete schedule for a student.
     * Provides the student's timetable
     *
     * @param studentId the unique identifier of the Student
     * @return the Schedule entity containing the student's timetable
     * @throws RuntimeException if the student is not found or has no schedule
     */
    public Schedule getStudentSchedule(String studentId) {
        return scheduleService.searchScheduleByStudentId(studentId);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}