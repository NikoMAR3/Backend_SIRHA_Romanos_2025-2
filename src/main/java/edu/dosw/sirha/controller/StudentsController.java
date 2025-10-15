package edu.dosw.sirha.controller;

import edu.dosw.sirha.controller.dtos.*;
import edu.dosw.sirha.model.components.util.AuthValidationUtils;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.AuthenticationService;
import edu.dosw.sirha.model.services.StudentService;
import edu.dosw.sirha.model.services.PetitionService;
import edu.dosw.sirha.model.persistence.repository.AcademicProgramRepository;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for student management.
 * Provides endpoints for CRUD operations, academic queries,
 * schedule management and petition tracking.
 */
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students Management", description = "Endpoints for student management")
public class StudentsController {

    private static final Logger logger = LoggerFactory.getLogger(StudentsController.class);

    private final StudentService studentService;
    private final AuthenticationService authenticationService;
    private final PetitionService petitionService;
    private final AcademicProgramRepository academicProgramRepository;
    private final DeaneryRepository deaneryRepository;

    public StudentsController(StudentService studentService,
                              AuthenticationService authenticationService,
                              PetitionService petitionService,
                              AcademicProgramRepository academicProgramRepository,
                              DeaneryRepository deaneryRepository) {
        this.studentService = studentService;
        this.authenticationService = authenticationService;
        this.petitionService = petitionService;
        this.academicProgramRepository = academicProgramRepository;
        this.deaneryRepository = deaneryRepository;
    }

    /**
     * Registers a new student in the system.
     * Only accessible for users with ACADEMIC_VICEPRESIDENT role.
     *
     * @param request student data to register
     * @param session HTTP session for authentication validation
     * @return registered student with code 201, or error with code 400/401/403
     */
    @PostMapping("/register")
    @Operation(summary = "Register new student")
    public ResponseEntity<?> registerStudent(
            @Valid @RequestBody StudentsRequestDTO request,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            return authCheck;
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (authenticationService.userExistsByDocument(request.getDocument())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthDto.ApiResponse(false, "A user with that document already exists"));
        }

        if (authenticationService.userExistsByEmail(request.getMail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthDto.ApiResponse(false, "A user with that email already exists"));
        }

        if (request.getAcademicProgramId() != null) {
            if (!academicProgramRepository.existsById(request.getAcademicProgramId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthDto.ApiResponse(false, "Academic program not found"));
            }
        }

        if (request.getDeaneryId() != null) {
            if (!deaneryRepository.existsById(request.getDeaneryId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthDto.ApiResponse(false, "Deanery not found"));
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());
        userDTO.setType(UserType.STUDENT);

        Student student = studentService.createStudent(userDTO);

        if (request.getStudentCode() != null) {
            student.setStudentCode(request.getStudentCode());
        }
        if (request.getSemester() != null) {
            student.setSemester(request.getSemester());
        }
        if (request.getAcademicStatus() != null) {
            student.setAcademicStatus(request.getAcademicStatus());
        }

        StudentsResponseDTO response = buildStudentResponse(student);
        logger.info("Student created successfully with ID: {} by user: {}", student.getId(), currentUser.getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all students in the system.
     * Accessible for ACADEMIC_VICEPRESIDENT and DEAN.
     *
     * @param session HTTP session for authentication validation
     * @return list of students with code 200, or error with code 401/403
     */
    @GetMapping
    @Operation(summary = "Get all students")
    public ResponseEntity<?> getAllStudents(HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        List<Student> students = studentService.searchAllStudents();
        List<StudentsResponseDTO> response = students.stream()
                .map(this::buildStudentResponse)
                .collect(Collectors.toList());

        logger.info("Retrieved {} students", students.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a student by their ID.
     * Students can only access their own data.
     *
     * @param id student identifier
     * @param session HTTP session for validation
     * @return requested student with code 200, or error with code 401/403/404
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<?> getStudentById(@PathVariable String id, HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            return authCheck;
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (!authenticationService.canAccessUserData(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthDto.ApiResponse(false, "You don't have permission to access this data"));
        }

        try {
            Student student = studentService.searchStudentById(id);
            StudentsResponseDTO response = buildStudentResponse(student);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthDto.ApiResponse(false, "Student not found"));
        }
    }

    /**
     * Updates an existing student's data.
     * Only accessible for ACADEMIC_VICEPRESIDENT.
     *
     * @param id student identifier
     * @param request updated data
     * @param session HTTP session for validation
     * @return updated student with code 200, or error with code 401/403/404
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update student")
    public ResponseEntity<?> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody StudentsRequestDTO request,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            return authCheck;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setName(request.getName());
        userDTO.setMail(request.getMail());
        userDTO.setDocument(request.getDocument());

        Optional<Student> updated = studentService.modifyStudent(id, userDTO);

        if (updated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthDto.ApiResponse(false, "Student not found"));
        }

        StudentsResponseDTO response = buildStudentResponse(updated.get());
        logger.info("Student updated with ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a student from the system.
     * Only accessible for ACADEMIC_VICEPRESIDENT.
     *
     * @param id student identifier
     * @param session HTTP session for validation
     * @return success message with code 200, or error with code 401/403/404
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student")
    public ResponseEntity<?> deleteStudent(@PathVariable String id, HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT);
        if (authCheck != null) {
            return authCheck;
        }

        boolean deleted = studentService.deleteStudent(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthDto.ApiResponse(false, "Student not found"));
        }

        logger.info("Student deleted with ID: {}", id);
        return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Student deleted successfully"));
    }

    /**
     * Searches students by name.
     * Accessible for ACADEMIC_VICEPRESIDENT and DEAN.
     *
     * @param name name or partial name to search
     * @param session HTTP session for validation
     * @return list of matching students with code 200
     */
    @GetMapping("/search")
    @Operation(summary = "Search students by name")
    public ResponseEntity<?> searchStudentsByName(
            @RequestParam String name,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        List<Student> students = studentService.searchStudentsByName(name);
        List<StudentsResponseDTO> response = students.stream()
                .map(this::buildStudentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves students by academic program.
     * Accessible for ACADEMIC_VICEPRESIDENT and DEAN.
     *
     * @param programName program name
     * @param session HTTP session for validation
     * @return list of students in the program with code 200
     */
    @GetMapping("/program/{programName}")
    @Operation(summary = "Get students by academic program")
    public ResponseEntity<?> getStudentsByProgram(
            @PathVariable String programName,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        List<Student> students = studentService.searchStudentsByProgram(programName);
        List<StudentsResponseDTO> response = students.stream()
                .map(this::buildStudentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves students by academic status.
     * Accessible for ACADEMIC_VICEPRESIDENT and DEAN.
     *
     * @param status academic status
     * @param session HTTP session for validation
     * @return list of students with the specified status with code 200
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get students by academic status")
    public ResponseEntity<?> getStudentsByStatus(
            @PathVariable AcademicStatus status,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        List<Student> students = studentService.searchStudentsByStatus(status);
        List<StudentsResponseDTO> response = students.stream()
                .map(this::buildStudentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a student's schedule.
     *
     * @param id student identifier
     * @param session HTTP session for validation
     * @return student schedule with code 200
     */
    @GetMapping("/{id}/schedule")
    @Operation(summary = "Get student schedule")
    public ResponseEntity<?> getStudentSchedule(@PathVariable String id, HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            return authCheck;
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (!authenticationService.canAccessUserData(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthDto.ApiResponse(false, "You don't have permission to access this data"));
        }

        try {
            Schedule schedule = studentService.getStudentSchedule(id);
            return ResponseEntity.ok(schedule);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthDto.ApiResponse(false, "Schedule not found"));
        }
    }

    /**
     * Retrieves a student's grade point average (GPA).
     *
     * @param id student identifier
     * @param session HTTP session for validation
     * @return student GPA with code 200
     */
    @GetMapping("/{id}/gpa")
    @Operation(summary = "Get student GPA")
    public ResponseEntity<?> getStudentGPA(@PathVariable String id, HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            return authCheck;
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (!authenticationService.canAccessUserData(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthDto.ApiResponse(false, "You don't have permission to access this data"));
        }

        try {
            double gpa = studentService.calculateGPA(id);
            Map<String, Object> response = new HashMap<>();
            response.put("studentId", id);
            response.put("gpa", gpa);
            response.put("calculatedAt", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthDto.ApiResponse(false, "Could not calculate GPA"));
        }
    }

    /**
     * Enrolls a student in a course.
     *
     * @param id student identifier
     * @param courseId course identifier
     * @param session HTTP session for validation
     * @return success message with code 200
     */
    @PostMapping("/{id}/enroll")
    @Operation(summary = "Enroll student in course")
    public ResponseEntity<?> enrollInCourse(
            @PathVariable String id,
            @RequestParam String courseId,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            studentService.enrollInCourse(id, courseId);
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Student enrolled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Withdraws a student from a course.
     *
     * @param id student identifier
     * @param courseId course identifier
     * @param session HTTP session for validation
     * @return success message with code 200
     */
    @DeleteMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw student from course")
    public ResponseEntity<?> withdrawFromCourse(
            @PathVariable String id,
            @RequestParam String courseId,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            studentService.withdrawFromCourse(id, courseId);
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Student withdrawn successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Retrieves a student's petitions.
     *
     * @param id student identifier
     * @param session HTTP session for validation
     * @return list of petitions with code 200
     */
    @GetMapping("/{id}/petitions")
    @Operation(summary = "Get student petitions")
    public ResponseEntity<?> getStudentPetitions(@PathVariable String id, HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(session);
        if (authCheck != null) {
            return authCheck;
        }

        User currentUser = AuthValidationUtils.getCurrentUser(session);

        if (!authenticationService.canAccessUserData(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthDto.ApiResponse(false, "You don't have permission to access this data"));
        }

        List<Petition> petitions = petitionService.searchPetitionsByStudentId(id);
        return ResponseEntity.ok(petitions);
    }

    /**
     * Updates a student's academic status.
     *
     * @param id student identifier
     * @param status new academic status
     * @param session HTTP session for validation
     * @return updated student with code 200
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update student academic status")
    public ResponseEntity<?> updateStudentStatus(
            @PathVariable String id,
            @RequestParam AcademicStatus status,
            HttpSession session) {

        ResponseEntity<?> authCheck = AuthValidationUtils.validateAuthentication(
                session, UserType.ACADEMIC_VICEPRESIDENT, UserType.DEAN);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            Student updated = studentService.updateAcademicStatus(id, status);
            StudentsResponseDTO response = buildStudentResponse(updated);

            logger.info("Academic status updated for student ID: {} to {}", id, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthDto.ApiResponse(false, "Student not found"));
        }
    }

    /**
     * Builds a response DTO from a Student entity.
     *
     * @param student Student entity
     * @return DTO with student information
     */
    private StudentsResponseDTO buildStudentResponse(Student student) {
        StudentsResponseDTO dto = new StudentsResponseDTO();
        dto.setName(student.getName());
        dto.setMail(student.getMail());
        dto.setDocument(student.getDocument());
        dto.setStudentCode(student.getStudentCode());
        dto.setSemester(student.getSemester());
        dto.setAcademicStatus(student.getAcademicStatus());
        dto.setRegistrationDate(student.getCreatedAt());
        dto.setLastUpdateDate(student.getLastLogin());

        if (student.getAcademicProgram() != null) {
            Map<String, Object> program = new HashMap<>();
            program.put("id", student.getAcademicProgram().getId());
            program.put("name", student.getAcademicProgram().getName());
            dto.setAcademicProgram(program);
        }

        if (student.getDeanery() != null) {
            Map<String, Object> deanery = new HashMap<>();
            deanery.put("id", student.getDeanery().getId());
            deanery.put("name", student.getDeanery().getDeaneryName());
            dto.setDeanery(deanery);
        }

        if (student.getTrafficLight() != null) {
            Map<String, Object> trafficLight = new HashMap<>();
            trafficLight.put("status", student.getTrafficLight().getStatus());
            trafficLight.put("grade", student.getTrafficLight().getGrade());
            trafficLight.put("credits", student.getTrafficLight().getCredits());
            dto.setTrafficLight(trafficLight);
        }

        return dto;
    }
}
