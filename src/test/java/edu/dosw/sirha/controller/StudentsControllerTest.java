package edu.dosw.sirha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.controller.dtos.StudentsRequestDTO;
import edu.dosw.sirha.model.persistence.repository.AcademicProgramRepository;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import edu.dosw.sirha.model.services.AuthenticationService;
import edu.dosw.sirha.model.services.PetitionService;
import edu.dosw.sirha.model.services.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static edu.dosw.sirha.model.entities.TrafficLightStatus.GREEN;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentsController.class)
@Import(StudentsControllerTest.MockBeansConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentService studentService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PetitionService petitionService;
    @Autowired
    private AcademicProgramRepository academicProgramRepository;
    @Autowired
    private DeaneryRepository deaneryRepository;

    @BeforeEach
    void setUp() {
        Mockito.reset(studentService, authenticationService, petitionService, academicProgramRepository, deaneryRepository);
        when(academicProgramRepository.findById("prog123")).thenReturn(Optional.of(new AcademicProgram()));
    }

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        // AcademicVicePresident es una subclase de User, debes tenerla en tu modelo.
        var adminUser = new edu.dosw.sirha.model.entities.AcademicVicePresident(
                "admin-123", "Admin", "admin@university.edu", "1234567890");
        session.setAttribute("user", adminUser);
        return session;
    }

    private MockHttpSession createStudentSession(String studentId) {
        MockHttpSession session = new MockHttpSession();
        Student student = new Student(studentId, "Student", "student@university.edu", "1111111111");
        session.setAttribute("user", student);
        return session;
    }

    @Test
    void registerStudent_ValidRequest_ReturnsCreated() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("John Doe");
        request.setMail("john@university.edu");
        request.setDocument("1234567890");
        request.setStudentCode("2024001");
        request.setSemester(5);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        // StudentCode mapea con el id (ver tu entidad)
        Student student = new Student();
        student.setStudentCode("2024001");
        student.setName("John Doe");
        student.setMail("john@university.edu");
        student.setDocument("1234567890");
        student.setSemester(5);
        student.setAcademicStatus(edu.dosw.sirha.model.entities.AcademicStatus.ACTIVE);

        when(studentService.createStudent(any())).thenReturn(student);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.mail").value("john@university.edu"))
                .andExpect(jsonPath("$.studentCode").value("2024001"));
    }

    @Test
    void getStudentById_ExistingStudent_ReturnsStudent() throws Exception {
        Student student = new Student();
        student.setStudentCode("student-123");
        student.setName("John Doe");
        student.setMail("john@university.edu");
        student.setDocument("1234567890");
        student.setSemester(5);
        student.setAcademicStatus(edu.dosw.sirha.model.entities.AcademicStatus.ACTIVE);

        when(studentService.searchStudentById("student-123")).thenReturn(student);
        when(authenticationService.canAccessUserData(any(Student.class), eq("student-123"))).thenReturn(true);

        mockMvc.perform(get("/api/students/student-123")
                        .session(createStudentSession("student-123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.studentCode").value("student-123"));
    }

    @Test
    void getStudentById_Forbidden_Returns403() throws Exception {
        when(authenticationService.canAccessUserData(any(Student.class), eq("student-123"))).thenReturn(false);

        mockMvc.perform(get("/api/students/student-123")
                        .session(createStudentSession("different-student-456")))
                .andExpect(status().isForbidden());
    }
    @Test
    void registerStudent_MissingRequiredField_ReturnsBadRequest() throws Exception {
        // Falta el campo 'name'
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setMail("john@university.edu");
        request.setDocument("1234567890");
        request.setStudentCode("2024001");
        request.setSemester(5);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerStudent_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("John Doe");
        request.setMail("no-es-email");
        request.setDocument("1234567890");
        request.setStudentCode("2024001");
        request.setSemester(5);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStudentById_StudentTriesToViewOtherStudent_ReturnsForbidden() throws Exception {
        // Simula que el estudiante autenticado es distinto al consultado
        when(authenticationService.canAccessUserData(any(Student.class), eq("student-999"))).thenReturn(false);

        mockMvc.perform(get("/api/students/student-999")
                        .session(createStudentSession("student-other")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStudentById_AcademicVicePresidentStudentNotFound_Returns404() throws Exception {
        when(studentService.searchStudentById("not-exist")).thenReturn(null);
        // Simula que sí puede acceder (es admin)
        when(authenticationService.canAccessUserData(any(edu.dosw.sirha.model.entities.AcademicVicePresident.class), eq("not-exist"))).thenReturn(true);

        mockMvc.perform(get("/api/students/not-exist")
                        .session(createAdminSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerStudent_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("John Doe");
        request.setMail("john@university.edu");
        request.setDocument("1234567890");
        request.setStudentCode("2024001");
        request.setSemester(5);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        when(studentService.createStudent(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getStudentById_OtherStudent_ReturnsForbidden() throws Exception {
        // El usuario autenticado no coincide con el id consultado
        when(authenticationService.canAccessUserData(any(Student.class), eq("student-999"))).thenReturn(false);

        mockMvc.perform(get("/api/students/student-999")
                        .session(createStudentSession("student-other")))
                .andExpect(status().isForbidden());
    }
    @Test
    void registerStudent_InvalidDocument_ReturnsBadRequest() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("John Doe");
        request.setMail("john@university.edu");
        request.setDocument("1234"); // Solo 4 dígitos, inválido
        request.setStudentCode("2024001");
        request.setSemester(5);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void registerStudent_SemesterTooLow_ReturnsBadRequest() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("John Doe");
        request.setMail("john@university.edu");
        request.setDocument("1234567890");
        request.setStudentCode("2024001");
        request.setSemester(0); // inválido
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerStudent_ValidRequest_ResponseFieldsOk() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("Ana Maria");
        request.setMail("ana@university.edu");
        request.setDocument("9876543210");
        request.setStudentCode("2024002");
        request.setSemester(7);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        Student student = new Student();
        student.setStudentCode("2024002");
        student.setName("Ana Maria");
        student.setMail("ana@university.edu");
        student.setDocument("9876543210");
        student.setSemester(7);
        student.setAcademicStatus(edu.dosw.sirha.model.entities.AcademicStatus.ACTIVE);

        when(studentService.createStudent(any())).thenReturn(student);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana Maria"))
                .andExpect(jsonPath("$.mail").value("ana@university.edu"))
                .andExpect(jsonPath("$.studentCode").value("2024002"))
                .andExpect(jsonPath("$.semester").value(7));
    }
    @Test
    void getStudentById_WithoutSession_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/students/student-123"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void registerStudent_DuplicateEmail_ReturnsConflict() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("Pepe");
        request.setMail("pepe@university.edu");
        request.setDocument("1234567890");
        request.setStudentCode("2024003");
        request.setSemester(3);
        request.setAcademicStatus(AcademicStatus.ACTIVE);

        // Simula error de base de datos (por ejemplo, email duplicado)
        when(studentService.createStudent(any())).thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate email"));

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    @Test
    void registerStudent_ExtraField_IgnoredAndCreated() throws Exception {
        String body = """
    {
      "name": "Nuevo Estudiante",
      "mail": "nuevo@university.edu",
      "document": "1234567890",
      "studentCode": "2024004",
      "semester": 2,
      "academicStatus": "ACTIVE",
      "campoExtra": "debe ser ignorado"
    }
    """;

        Student student = new Student();
        student.setStudentCode("2024004");
        student.setName("Nuevo Estudiante");
        student.setMail("nuevo@university.edu");
        student.setDocument("1234567890");
        student.setSemester(2);
        student.setAcademicStatus(edu.dosw.sirha.model.entities.AcademicStatus.ACTIVE);

        when(studentService.createStudent(any())).thenReturn(student);

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nuevo Estudiante"));
    }

    @Test
    void registerStudent_WeakPassword_ReturnsBadRequest() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("Weak Pass");
        request.setMail("weak@university.edu");
        request.setDocument("5555555555");
        request.setStudentCode("2024006");
        request.setSemester(2);
        request.setAcademicStatus(AcademicStatus.ACTIVE);
        request.setPassword("123"); // Muy corto, inseguro

        // Simula que tu servicio lanza una IllegalArgumentException por password débil
        when(studentService.createStudent(any())).thenThrow(new IllegalArgumentException("Password is too weak"));

        mockMvc.perform(post("/api/students/register")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getStudentById_WithSpecialChars_ReturnsNotFound() throws Exception {
        String studentId = "id-con-guion_bajo.2025";
        when(authenticationService.canAccessUserData(any(edu.dosw.sirha.model.entities.AcademicVicePresident.class), eq(studentId))).thenReturn(true);
        when(studentService.searchStudentById(studentId)).thenReturn(null);

        mockMvc.perform(get("/api/students/" + studentId)
                        .session(createAdminSession()))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean @Primary
        public StudentService studentService() {
            return Mockito.mock(StudentService.class);
        }
        @Bean @Primary
        public AuthenticationService authenticationService() {
            return Mockito.mock(AuthenticationService.class);
        }
        @Bean @Primary
        public PetitionService petitionService() {
            return Mockito.mock(PetitionService.class);
        }
        @Bean @Primary
        public AcademicProgramRepository academicProgramRepository() { return Mockito.mock(AcademicProgramRepository.class); }
        @Bean @Primary
        public DeaneryRepository deaneryRepository() { return Mockito.mock(DeaneryRepository.class); }
    }

    @Test
    void getStudentGPA_Success() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(studentService.calculateGPA("student-123")).thenReturn(4.3);

        mockMvc.perform(get("/api/students/student-123/gpa")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("student-123"))
                .andExpect(jsonPath("$.gpa").value(4.3));
    }
    @Test
    void updateStudent_Success() throws Exception {
        StudentsRequestDTO dto = new StudentsRequestDTO();
        dto.setName("Nuevo Nombre");
        dto.setMail("nuevo@email.com");      // <- Obligatorio
        dto.setDocument("1234567890");       // <- Obligatorio
        // ...otros campos...

        Student updated = new Student();
        updated.setName("Nuevo Nombre");
        updated.setMail("nuevo@email.com");
        updated.setDocument("1234567890");
        // ...otros campos...

        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(studentService.modifyStudent(eq("student-123"), any())).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/students/student-123")
                        .session(createAdminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nuevo Nombre"));
    }

    @Test
    void deleteStudent_Success() throws Exception {
        when(studentService.deleteStudent("student-123")).thenReturn(true);
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);

        mockMvc.perform(delete("/api/students/student-123")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student deleted successfully"));
    }

    @Test
    void getAllStudents_Success() throws Exception {
        List<Student> students = List.of(new Student(), new Student());
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/api/students")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentSchedule_Success() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(studentService.getStudentSchedule("student-123")).thenReturn(new Schedule());

        mockMvc.perform(get("/api/students/student-123/schedule")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void updateStudentStatus_Success() throws Exception {
        Student updated = new Student();
        updated.setStudentCode("student-123");
        updated.setAcademicStatus(AcademicStatus.INACTIVE);

        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(studentService.updateAcademicStatus("student-123", AcademicStatus.INACTIVE)).thenReturn(updated);

        mockMvc.perform(patch("/api/students/student-123/status")
                        .param("status", "INACTIVE")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentCode").value("student-123"))
                .andExpect(jsonPath("$.academicStatus").value("INACTIVE"));
    }

    @Test
    void enrollInCourse_Success() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        doNothing().when(studentService).enrollInCourse("student-123", "COURSE-1");

        mockMvc.perform(post("/api/students/student-123/enroll")
                        .param("courseId", "COURSE-1")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void withdrawFromCourse_Success() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        doNothing().when(studentService).withdrawFromCourse("student-123", "COURSE-1");

        mockMvc.perform(delete("/api/students/student-123/withdraw")
                        .param("courseId", "COURSE-1")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void withdrawFromCourse_CourseNotFound_Returns404() throws Exception {
        doThrow(new IllegalArgumentException("Course not found"))
                .when(studentService).withdrawFromCourse("student-123", "COURSE-XXX");
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);

        mockMvc.perform(delete("/api/students/student-123/withdraw")
                        .param("courseId", "COURSE-XXX")
                        .session(createAdminSession()))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getStudentPetitions_Success() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(petitionService.getStudentPetitions("student-123")).thenReturn(List.of());

        mockMvc.perform(get("/api/students/student-123/petitions")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void searchStudentsByName_Success() throws Exception {
        when(studentService.searchStudentsByName("Ana")).thenReturn(List.of(new Student()));
        mockMvc.perform(get("/api/students/search")
                        .param("name", "Ana")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentsByProgram_Success() throws Exception {
        List<Student> students = List.of(new Student(), new Student());
        when(studentService.searchStudentsByProgram("Ingeniería")).thenReturn(students);

        mockMvc.perform(get("/api/students/program/Ingeniería")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentsByStatus_Success() throws Exception {
        List<Student> students = List.of(new Student());
        when(studentService.searchStudentsByStatus(AcademicStatus.ACTIVE)).thenReturn(students);

        mockMvc.perform(get("/api/students/status/ACTIVE")
                        .session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentsByProgram_Forbidden() throws Exception {
        mockMvc.perform(get("/api/students/program/Ingeniería")
                        .session(createStudentSession("Niko")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStudentsByStatus_Forbidden() throws Exception {
        mockMvc.perform(get("/api/students/status/ACTIVE")
                        .session(createStudentSession("Niko")))
                .andExpect(status().isForbidden());
    }

    // Test para getStudentSchedule - estudiante no encontrado
    @Test
    void getStudentSchedule_NotFound() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(studentService.getStudentSchedule("student-123")).thenThrow(new RuntimeException("Schedule not found"));

        mockMvc.perform(get("/api/students/student-123/schedule")
                        .session(createAdminSession()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Schedule not found"));
    }

    // Test para getStudentSchedule - sin permiso
    @Test
    void getStudentSchedule_Forbidden() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(false);

        mockMvc.perform(get("/api/students/student-123/schedule")
                        .session(createAdminSession()))
                .andExpect(status().isForbidden());
    }

    // Test para getStudentGPA - error de cálculo
    @Test
    void getStudentGPA_CalculationError() throws Exception {
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);
        when(studentService.calculateGPA("student-123")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/students/student-123/gpa")
                        .session(createAdminSession()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // Test para updateStudentStatus - estudiante no encontrado
    @Test
    void updateStudentStatus_NotFound() throws Exception {
        when(studentService.updateAcademicStatus("student-123", AcademicStatus.INACTIVE))
                .thenThrow(new RuntimeException("Student not found"));
        when(authenticationService.canAccessUserData(any(), eq("student-123"))).thenReturn(true);

        mockMvc.perform(patch("/api/students/student-123/status")
                        .param("status", "INACTIVE")
                        .session(createAdminSession()))
                .andExpect(status().isNotFound());
    }
    @Test
    void getStudentById_withAllFields() throws Exception {
        // Mock Student with all fields present
        Student student = new Student();
        student.setId("id1");
        student.setName("Ana");
        student.setMail("ana@uni.edu");
        student.setDocument("123");
        student.setStudentCode("S2025");
        student.setSemester(7);
        student.setAcademicStatus(AcademicStatus.ACTIVE);
        student.setCreatedAt(LocalDateTime.now());
        student.setLastLogin(LocalDateTime.now());

        AcademicProgram program = new AcademicProgram();
        program.setId("prog1");
        program.setName("Ingeniería");
        student.setAcademicProgram(program);

        Deanery deanery = new Deanery();
        deanery.setId("dean1");
        deanery.setDeaneryName("Ciencias");
        student.setDeanery(deanery);

        TrafficLight traffic = new TrafficLight();
        traffic.setStatus(GREEN);
        traffic.setGrade(4.5);
        traffic.setCredits(120);
        student.setTrafficLight(traffic);

        when(authenticationService.canAccessUserData(any(), eq("id1"))).thenReturn(true);
        when(studentService.searchStudentById("id1")).thenReturn(student);

        mockMvc.perform(get("/api/students/id1")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.academicProgram.id").value("prog1"))
                .andExpect(jsonPath("$.deanery.id").value("dean1"))
                .andExpect(jsonPath("$.trafficLight.status").value("GREEN"));
    }

    @Test
    void getStudentById_withoutOptionalFields() throws Exception {
        // Student with all optional fields null
        Student student = new Student();
        student.setId("id2");
        student.setName("Pepe");
        student.setMail("pepe@uni.edu");
        student.setDocument("888");

        when(authenticationService.canAccessUserData(any(), eq("id2"))).thenReturn(true);
        when(studentService.searchStudentById("id2")).thenReturn(student);

        mockMvc.perform(get("/api/students/id2")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pepe"))
                .andExpect(jsonPath("$.academicProgram").doesNotExist())
                .andExpect(jsonPath("$.deanery").doesNotExist())
                .andExpect(jsonPath("$.trafficLight").doesNotExist());
    }

    @Test
    void getStudentById_onlyProgram() throws Exception {
        Student student = new Student();
        student.setId("id3");
        student.setName("Luisa");
        AcademicProgram program = new AcademicProgram();
        program.setId("prog2");
        program.setName("Medicina");
        student.setAcademicProgram(program);

        when(authenticationService.canAccessUserData(any(), eq("id3"))).thenReturn(true);
        when(studentService.searchStudentById("id3")).thenReturn(student);

        mockMvc.perform(get("/api/students/id3")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicProgram.id").value("prog2"))
                .andExpect(jsonPath("$.deanery").doesNotExist())
                .andExpect(jsonPath("$.trafficLight").doesNotExist());
    }

    @Test
    void getStudentById_onlyDeanery() throws Exception {
        Student student = new Student();
        student.setId("id4");
        student.setName("Carlos");
        Deanery deanery = new Deanery();
        deanery.setId("d2");
        deanery.setDeaneryName("Humanidades");
        student.setDeanery(deanery);

        when(authenticationService.canAccessUserData(any(), eq("id4"))).thenReturn(true);
        when(studentService.searchStudentById("id4")).thenReturn(student);

        mockMvc.perform(get("/api/students/id4")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deanery.id").value("d2"))
                .andExpect(jsonPath("$.academicProgram").doesNotExist())
                .andExpect(jsonPath("$.trafficLight").doesNotExist());
    }
    @Test
    void registerStudent_Success() throws Exception {
        StudentsRequestDTO dto = new StudentsRequestDTO();
        dto.setName("Ana");
        dto.setMail("ana@uni.edu");
        dto.setDocument("1234567890");
        dto.setAcademicProgramId("prog1");
        dto.setDeaneryId("dean1");

        when(authenticationService.userExistsByDocument("1234567890")).thenReturn(false);
        when(authenticationService.userExistsByEmail("ana@uni.edu")).thenReturn(false);
        when(academicProgramRepository.existsById("prog1")).thenReturn(true);
        when(deaneryRepository.existsById("dean1")).thenReturn(true);

        Student student = new Student();
        student.setName("Ana");
        when(studentService.createStudent(any(UserDTO.class))).thenReturn(student);

        mockMvc.perform(post("/api/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .session(createAdminSession()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void registerStudent_ExistingDocument_Returns400() throws Exception {
        StudentsRequestDTO dto = new StudentsRequestDTO();
        dto.setName("Ana");
        dto.setMail("ana@uni.edu");
        dto.setDocument("1234567890");

        when(authenticationService.userExistsByDocument("1234567890")).thenReturn(true);

        mockMvc.perform(post("/api/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .session(createAdminSession()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A user with that document already exists"));
    }

    @Test
    void registerStudent_ExistingEmail_Returns400() throws Exception {
        StudentsRequestDTO dto = new StudentsRequestDTO();
        dto.setName("Ana");
        dto.setMail("ana@uni.edu");
        dto.setDocument("1234567890");

        when(authenticationService.userExistsByDocument("1234567890")).thenReturn(false);
        when(authenticationService.userExistsByEmail("ana@uni.edu")).thenReturn(true);

        mockMvc.perform(post("/api/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .session(createAdminSession()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A user with that email already exists"));
    }

    @Test
    void registerStudent_NonexistentAcademicProgram_Returns400() throws Exception {
        StudentsRequestDTO dto = new StudentsRequestDTO();
        dto.setName("Ana");
        dto.setMail("ana@uni.edu");
        dto.setDocument("1234567890");
        dto.setAcademicProgramId("progNotExist");

        when(authenticationService.userExistsByDocument("123")).thenReturn(false);
        when(authenticationService.userExistsByEmail("ana@uni.edu")).thenReturn(false);
        when(academicProgramRepository.existsById("progNotExist")).thenReturn(false);

        mockMvc.perform(post("/api/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .session(createAdminSession()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Academic program not found"));
    }
}