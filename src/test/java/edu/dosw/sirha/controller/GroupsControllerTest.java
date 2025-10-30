package edu.dosw.sirha.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.ClassSessionService;
import edu.dosw.sirha.model.services.SubjectService;
import edu.dosw.sirha.model.services.ProfessorService;
import edu.dosw.sirha.model.services.PetitionService;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupsController.class)
@Import(GroupsControllerTest.MockBeansConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ClassSessionService classSessionService;
    @Autowired private SubjectService subjectService;
    @Autowired private ProfessorService professorService;
    @Autowired private PetitionService petitionService;

    @BeforeEach
    void setUp() {
        Mockito.reset(classSessionService, subjectService, professorService, petitionService);
    }

    // ----- Sesión helpers -----
    private MockHttpSession deanSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new Dean("dean-1", "Dean Name", "dean@uni.edu", "123456"));
        return session;
    }
    private MockHttpSession viceSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new AcademicVicePresident("avp-1", "AVP", "avp@uni.edu", "654321"));
        return session;
    }
    private MockHttpSession profSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new Professor("prof-1", "Prof Name", "prof@uni.edu", "987654"));
        return session;
    }
    private MockHttpSession studentSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new Student("prof-1", "student Name", "stud@uni.edu", "1237654"));
        return session;
    }

    // ----------- TESTS CRUD GROUPS ------------

    @Test
    void createGroup_validRequest_dean_returnsCreated() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Grupo 101");
        req.setSubjectId("SUBJ-1");
        req.setMaxStudents(35);

        Subject subj = new Subject();
        subj.setId("SUBJ-1");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(3);
        subj.setLevel(1);

        when(subjectService.searchSubjectById("SUBJ-1")).thenReturn(subj);

        ClassSession saved = new ClassSession();
        saved.setId("group-1");
        saved.setGroupName("Grupo 101");
        saved.setSubjectId("SUBJ-1");
        saved.setSubjectShortName("MAT");
        saved.setSubjectName("Matemáticas");
        saved.setSubjectCredits(3);
        saved.setSubjectLevel(1);
        saved.setCapacity(35);
        saved.setEnrolledStudents(0);
        saved.setStartDate(LocalDateTime.now());
        saved.setEndDate(LocalDateTime.now().plusMonths(6));

        when(classSessionService.createClassSession(any(ClassSession.class))).thenReturn(saved);

        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupName").value("Grupo 101"))
                .andExpect(jsonPath("$.maxStudents").value(35))
                .andExpect(jsonPath("$.subjectShortName").value("MAT"));
    }

    @Test
    void createGroup_invalidCapacity_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Grupo 105");
        req.setSubjectId("SUBJ-1");
        req.setMaxStudents(-5);

        Subject subj = new Subject();
        subj.setId("SUBJ-1");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(3);
        subj.setLevel(1);

        when(subjectService.searchSubjectById("SUBJ-1")).thenReturn(subj);
        // Simula que el service lanza error por capacidad inválida
        when(classSessionService.createClassSession(any())).thenThrow(new IllegalArgumentException("Capacity must be greater than 0"));

        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGroup_unauthorizedUser_returnsForbidden() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Grupo 102");
        req.setSubjectId("SUBJ-1");
        req.setMaxStudents(20);

        mockMvc.perform(post("/api/groups")
                        .session(profSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGroupById_existingGroup_returnsGroup() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("group-2");
        session.setGroupName("Grupo 102");
        session.setSubjectId("SUBJ-1");
        session.setSubjectShortName("MAT");
        session.setSubjectName("Matemáticas");
        session.setCapacity(30);
        session.setEnrolledStudents(10);

        when(classSessionService.searchSessionById("group-2")).thenReturn(session);

        mockMvc.perform(get("/api/groups/group-2")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value("group-2"))
                .andExpect(jsonPath("$.groupName").value("Grupo 102"));
    }

    @Test
    void getGroupById_noSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/groups/group-3"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGroupById_notFound_returnsNotFound() throws Exception {
        when(classSessionService.searchSessionById("group-10")).thenThrow(new IllegalArgumentException("Session not found"));
        mockMvc.perform(get("/api/groups/group-10")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup_existingGroup_returnsNoContent() throws Exception {
        doNothing().when(classSessionService).deleteClassSession("group-4");
        when(classSessionService.searchSessionById("group-4")).thenReturn(new ClassSession());

        mockMvc.perform(delete("/api/groups/group-4")
                        .session(viceSession()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteGroup_notFound_returnsNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Group not found")).when(classSessionService).deleteClassSession(anyString());
        mockMvc.perform(delete("/api/groups/group-404")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllGroups_returnsGroupsList() throws Exception {
        ClassSession s1 = new ClassSession(); s1.setId("g1"); s1.setGroupName("A"); s1.setCapacity(10);
        ClassSession s2 = new ClassSession(); s2.setId("g2"); s2.setGroupName("B"); s2.setCapacity(20);

        when(classSessionService.searchAllSessions()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/groups").session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupName").value("A"))
                .andExpect(jsonPath("$[1].groupName").value("B"));
    }

    @Test
    void updateGroupCapacity_validRequest_returnsOk() throws Exception {
        ClassSession session = new ClassSession();

        when(classSessionService.searchSessionById("g1")).thenReturn(session);
        when(classSessionService.updateClassSession(any(ClassSession.class))).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1/capacity")
                        .param("maxStudents", "40")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxStudents").value(40)); // el mock no cambia, solo para cubrir
    }

    @Test
    void updateGroupCapacity_invalidCapacity_returnsBadRequest() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g1"); session.setCapacity(30);

        when(classSessionService.searchSessionById("g1")).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1/capacity")
                        .param("maxStudents", "-1")
                        .session(deanSession()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateGroupCapacity_groupNotFound_returnsNotFound() throws Exception {
        when(classSessionService.searchSessionById(anyString())).thenReturn(null);
        mockMvc.perform(put("/api/groups/g1/capacity")
                        .param("maxStudents", "30")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }

    // --- Reporte de capacidad
    @Test
    void getGroupCapacity_returnsCapacityInfo() throws Exception {
        ClassSessionService.EnrollmentStats stats = new ClassSessionService.EnrollmentStats(27, 30, 2, 3);

        when(classSessionService.getEnrollmentStats("g5")).thenReturn(stats);

        mockMvc.perform(get("/api/groups/g5/capacity").session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxStudents").value(30))
                .andExpect(jsonPath("$.currentStudents").value(27))
                .andExpect(jsonPath("$.availableSpots").value(3))
                .andExpect(jsonPath("$.waitingListCount").value(2));
    }

    // --- Asignar profesor
    @Test
    void assignProfessor_validRequest_returnsOk() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g8"); session.setProfessorCode(null);

        when(classSessionService.searchSessionById("g8")).thenReturn(session);
        when(classSessionService.updateClassSession(any(ClassSession.class))).thenReturn(session);

        mockMvc.perform(put("/api/groups/g8/professor")
                        .param("professorId", "prof-1")
                        .session(deanSession()))
                .andExpect(status().isOk());
    }

    // --- Tests de inscripción/retirar estudiante
    @Test
    void enrollStudent_valid_returnsOk() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g10");
        session.setEnrolledStudentIds(List.of("stu1"));
        session.setEnrolledStudents(1);
        session.setCapacity(30);
        when(classSessionService.enrollStudent("g10", "stu2")).thenReturn(session);

        mockMvc.perform(post("/api/groups/g10/students/stu2").session(deanSession()))
                .andExpect(status().isOk());
    }

    @Test
    void withdrawStudent_valid_returnsOk() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g11");
        session.setEnrolledStudentIds(List.of());
        session.setEnrolledStudents(0);
        session.setCapacity(30);
        when(classSessionService.withdrawStudent("g11", "stu2")).thenReturn(session);

        mockMvc.perform(delete("/api/groups/g11/students/stu2").session(deanSession()))
                .andExpect(status().isOk());
    }

    // --- Test para obtener horarios de grupo
    @Test
    void getGroupSchedules_returnsSchedules() throws Exception {
        ClassSchedule sched = new ClassSchedule("sch1", "MONDAY", LocalTime.of(8,0), LocalTime.of(10,0), "A101");
        when(classSessionService.searchSchedulesBySession("g12")).thenReturn(List.of(sched));

        mockMvc.perform(get("/api/groups/g12/schedules").session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].classroom").value("A101"));
    }

    // --- Test para reportes de grupos llenos
    @Test
    void getFullGroups_returnsOk() throws Exception {
        ClassSession full = new ClassSession(); full.setId("g13"); full.setGroupName("FullGroup");
        full.setCapacity(10); full.setEnrolledStudents(10);
        when(classSessionService.searchAllSessions()).thenReturn(List.of(full));
        mockMvc.perform(get("/api/groups/reports/full").session(deanSession()))
                .andExpect(status().isOk());
    }

    // --- Test para getGroupsByProfessor
    @Test
    void getGroupsByProfessor_returnsGroups() throws Exception {
        ClassSession cs = new ClassSession(); cs.setId("g16"); cs.setProfessorCode("prof-1");
        when(classSessionService.searchSessionsByProfessor("prof-1")).thenReturn(List.of(cs));
        mockMvc.perform(get("/api/groups/professor/prof-1").session(deanSession()))
                .andExpect(status().isOk());
    }

    // --- Test para búsqueda por materia
    @Test
    void getGroupsBySubject_returnsGroups() throws Exception {
        ClassSession cs = new ClassSession(); cs.setId("g17"); cs.setSubjectShortName("MAT");
        when(classSessionService.searchSessionsBySubjectShortName("MAT")).thenReturn(List.of(cs));
        mockMvc.perform(get("/api/groups/subject/MAT").session(deanSession()))
                .andExpect(status().isOk());
    }

    // Puedes agregar más tests para cubrir los endpoints de materias, profesores, reportes avanzados, schedules, etc.

    @TestConfiguration
    static class MockBeansConfig {
        @Bean @Primary public ClassSessionService classSessionService() { return Mockito.mock(ClassSessionService.class); }
        @Bean @Primary public SubjectService subjectService() { return Mockito.mock(SubjectService.class); }
        @Bean @Primary public ProfessorService professorService() { return Mockito.mock(ProfessorService.class); }
        @Bean @Primary public PetitionService petitionService() { return Mockito.mock(PetitionService.class); }
    }
    @Test
    void updateGlobalSchedule_noDaysUpdated_returnsBadRequest() throws Exception {
        GroupsRequestDTO.GlobalScheduleRequest req = new GroupsRequestDTO.GlobalScheduleRequest();
        req.setDays(List.of("LUNES", "MARTES"));
        req.setStartTime("07:00");
        req.setEndTime("09:00");
        req.setClassroom("A1");

        // El grupo existe pero NO tiene schedules
        ClassSession session = new ClassSession();
        session.setId("g20");
        session.setSchedules(List.of()); // vacío

        when(classSessionService.searchSessionById("g20")).thenReturn(session);

        mockMvc.perform(put("/api/groups/g20/schedule/global")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void removeProfessorFromGroup_professorCodeMismatch_returnsBadRequest() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g30");
        session.setProfessorCode("prof-abc");

        when(classSessionService.searchSessionById("g30")).thenReturn(session);

        mockMvc.perform(put("/api/groups/g30/professor/remove")
                        .param("professorCode", "otro-prof")
                        .session(deanSession()))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createSubject_missingFields_returnsBadRequest() throws Exception {
        GroupsRequestDTO.SubjectRequest req = new GroupsRequestDTO.SubjectRequest();
        // No subjectId, faltan campos obligatorios

        mockMvc.perform(post("/api/groups/subjects")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void updateSubject_notFound_returnsNotFound() throws Exception {
        GroupsRequestDTO.SubjectRequest req = new GroupsRequestDTO.SubjectRequest();
        req.setSubjectId("SUBJ-404");
        req.setSubjectShortName("MAT");
        req.setSubjectName("Matemáticas");
        req.setSubjectCredits(4);
        req.setSubjectLevel(1);

        when(subjectService.searchSubjectById("SUBJ-404")).thenReturn(null);

        mockMvc.perform(put("/api/groups/subjects/SUBJ-404")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
    @Test
    void deleteSubject_notFound_returnsNotFound() throws Exception {
        when(subjectService.searchSubjectById("SUBJ-404")).thenReturn(null);

        mockMvc.perform(delete("/api/groups/subjects/SUBJ-404")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }
    @Test
    void registerProfessor_validRequest_returnsCreated() throws Exception {
        GroupsRequestDTO.ProfessorRequest req = new GroupsRequestDTO.ProfessorRequest();
        req.setName("Prof Nuevo");
        req.setMail("nuevo@uni.edu");
        req.setDocument("1234567890"); // ← 10 dígitos válidos
        req.setProfessorCode("P001");

        Professor prof = new Professor();
        prof.setId("pid1");
        prof.setName("Prof Nuevo");
        prof.setProfessorCode("P001");

        when(professorService.createProfessor(any())).thenReturn(prof);
        when(professorService.save(any())).thenReturn(prof);

        mockMvc.perform(post("/api/groups/professors")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorCode").value("P001"));
    }

    @Test
    void registerProfessor_noPermissions_returnsForbidden() throws Exception {
        GroupsRequestDTO.ProfessorRequest req = new GroupsRequestDTO.ProfessorRequest();
        req.setName("Prof Nuevo");
        req.setMail("nuevo@uni.edu");
        req.setDocument("1234567890");
        req.setProfessorCode("P002");

        mockMvc.perform(post("/api/groups/professors")
                        .session(profSession()) // Un usuario sin permisos suficientes
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // TEST: createGroup - groupName vacío
    @Test
    void createGroup_groupNameBlank_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName(""); // <--- inválido, dispara @NotBlank
        req.setSubjectId("MAT-1");
        req.setMaxStudents(30);
        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // TEST: createGroup - subjectId null
    @Test
    void createGroup_subjectIdNull_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Grupo Test");
        req.setSubjectId(null); // <--- inválido, dispara @NotBlank
        req.setMaxStudents(30);
        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // TEST: createGroup - maxStudents fuera de rango
    @Test
    void createGroup_maxStudentsOutOfRange_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Grupo Test");
        req.setSubjectId("MAT-1");
        req.setMaxStudents(0); // <--- inválido, @Min(1)
        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // TEST: createSubject - faltan campos obligatorios
    @Test
    void createSubject_blankFields_returnsBadRequest() throws Exception {
        GroupsRequestDTO.SubjectRequest req = new GroupsRequestDTO.SubjectRequest();
        req.setSubjectId(""); // @NotBlank
        req.setSubjectShortName(""); // @NotBlank
        req.setSubjectName(""); // @NotBlank
        req.setSubjectCredits(null); // @NotNull
        req.setSubjectLevel(null); // @NotNull
        mockMvc.perform(post("/api/groups/subjects")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // TEST: registerProfessor - document formato incorrecto
    @Test
    void registerProfessor_invalidDocumentFormat_returnsBadRequest() throws Exception {
        GroupsRequestDTO.ProfessorRequest req = new GroupsRequestDTO.ProfessorRequest();
        req.setName("Prof");
        req.setMail("prof@uni.edu");
        req.setDocument("12345"); // <--- solo 5 dígitos, debe ser 10
        req.setProfessorCode("P001");
        mockMvc.perform(post("/api/groups/professors")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
    // ... imports, configuración y helpers iguales a tus tests previos

    @Test
    void getCapacitySummaryReport_returnsOk() throws Exception {
        ClassSession s1 = new ClassSession(); s1.setId("g1"); s1.setGroupName("A"); s1.setCapacity(10); s1.setEnrolledStudents(9);
        ClassSession s2 = new ClassSession(); s2.setId("g2"); s2.setGroupName("B"); s2.setCapacity(20); s2.setEnrolledStudents(20);
        when(classSessionService.searchAllSessions()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/groups/reports/capacity-summary")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGroups").value(2))
                .andExpect(jsonPath("$.fullGroups").value(1))
                .andExpect(jsonPath("$.totalEnrolled").value(29));
    }

    @Test
    void createSubject_validRequest_returnsCreated() throws Exception {
        GroupsRequestDTO.SubjectRequest req = new GroupsRequestDTO.SubjectRequest();
        req.setSubjectId("MAT-2025");
        req.setSubjectShortName("MAT");
        req.setSubjectName("Matemáticas");
        req.setSubjectCredits(4);
        req.setSubjectLevel(1);

        Subject subj = new Subject();
        subj.setId("MAT-2025");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(4);
        subj.setLevel(1);

        when(subjectService.createSubject(any())).thenReturn(subj);
        when(subjectService.searchSubjectById("MAT-2025")).thenReturn(subj);
        when(classSessionService.searchSessionsBySubjectShortName("MAT")).thenReturn(List.of());

        mockMvc.perform(post("/api/groups/subjects")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subjectId").value("MAT-2025"));
    }

    @Test
    void updateIndividualSchedule_valid_returnsOk() throws Exception {
        GroupsRequestDTO.ScheduleRequest req = new GroupsRequestDTO.ScheduleRequest();
        req.setDayOfWeek("LUNES");
        req.setStartTime("09:00");
        req.setEndTime("11:00");
        req.setClassroom("A1");

        ClassSchedule sched = new ClassSchedule("s1", "LUNES", LocalTime.of(9,0), LocalTime.of(11,0), "A1");
        ClassSession sess = new ClassSession();
        sess.setId("g10");
        sess.setSchedules(List.of(sched));
        when(classSessionService.searchSessionById("g10")).thenReturn(sess);
        when(classSessionService.updateClassSession(any())).thenReturn(sess);

        mockMvc.perform(put("/api/groups/g10/schedule")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void addGlobalScheduleToGroup_valid_returnsOk() throws Exception {
        GroupsRequestDTO.GlobalScheduleRequest req = new GroupsRequestDTO.GlobalScheduleRequest();
        req.setDays(List.of("LUNES", "MARTES"));
        req.setStartTime("07:00");
        req.setEndTime("09:00");
        req.setClassroom("A1");

        ClassSession session = new ClassSession();
        session.setId("g20");
        when(classSessionService.searchSessionById("g20")).thenReturn(session);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(post("/api/groups/g20/schedule/global")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfessorByCode_valid_returnsOk() throws Exception {
        GroupsRequestDTO.ProfessorRequest req = new GroupsRequestDTO.ProfessorRequest();
        req.setName("Prof Nuevo");
        req.setMail("nuevo@uni.edu");
        req.setDocument("1234567890");
        req.setProfessorCode("P002");

        Professor prof = new Professor(); prof.setProfessorCode("P002"); prof.setName("Prof Nuevo");
        when(professorService.searchProfessorByCode("P002")).thenReturn(prof);
        when(professorService.save(any())).thenReturn(prof);

        mockMvc.perform(put("/api/groups/professors/code/P002")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professorCode").value("P002"));
    }

    @Test
    void updateSubject_valid_returnsOk() throws Exception {
        GroupsRequestDTO.SubjectRequest req = new GroupsRequestDTO.SubjectRequest();
        req.setSubjectId("MAT-2025");
        req.setSubjectShortName("MAT");
        req.setSubjectName("Matemáticas");
        req.setSubjectCredits(4);
        req.setSubjectLevel(1);

        Subject subj = new Subject();
        subj.setId("MAT-2025");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(4);
        subj.setLevel(1);

        when(subjectService.searchSubjectById("MAT-2025")).thenReturn(subj);
        when(subjectService.save(any())).thenReturn(subj);
        when(classSessionService.searchSessionsBySubjectShortName("MAT")).thenReturn(List.of());

        mockMvc.perform(put("/api/groups/subjects/MAT-2025")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectId").value("MAT-2025"));
    }

    @Test
    void addScheduleToGroup_valid_returnsOk() throws Exception {
        GroupsRequestDTO.ScheduleRequest req = new GroupsRequestDTO.ScheduleRequest();
        req.setDayOfWeek("MIERCOLES");
        req.setStartTime("10:00");
        req.setEndTime("12:00");
        req.setClassroom("B2");

        ClassSession session = new ClassSession();
        session.setId("g30");
        when(classSessionService.searchSessionById("g30")).thenReturn(session);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(post("/api/groups/g30/schedule")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateGroup_valid_returnsOk() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Actualizado");
        req.setSubjectId("MAT-2025");
        req.setMaxStudents(40);

        ClassSession session = new ClassSession();
        session.setId("g40");
        session.setGroupName("Actualizado");
        session.setSubjectId("MAT-2025");
        session.setCapacity(40);

        // Mockear la materia para que sí exista
        Subject subj = new Subject();
        subj.setId("MAT-2025");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(4);
        subj.setLevel(1);
        when(subjectService.searchSubjectById("MAT-2025")).thenReturn(subj);

        when(classSessionService.searchSessionById("g40")).thenReturn(session);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(put("/api/groups/g40")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("Actualizado"))
                .andExpect(jsonPath("$.maxStudents").value(40));
    }

    @Test
    void updateGroupSchedules_valid_returnsOk() throws Exception {
        GroupsRequestDTO.ScheduleRequest s1 = new GroupsRequestDTO.ScheduleRequest();
        s1.setDayOfWeek("LUNES"); s1.setStartTime("08:00"); s1.setEndTime("10:00"); s1.setClassroom("A1");
        GroupsRequestDTO.ScheduleRequest s2 = new GroupsRequestDTO.ScheduleRequest();
        s2.setDayOfWeek("VIERNES"); s2.setStartTime("10:00"); s2.setEndTime("12:00"); s2.setClassroom("B2");

        List<GroupsRequestDTO.ScheduleRequest> reqList = List.of(s1, s2);

        ClassSession session = new ClassSession();
        session.setId("g50");
        when(classSessionService.searchSessionById("g50")).thenReturn(session);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(put("/api/groups/g50/schedules")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqList)))
                .andExpect(status().isOk());
    }

    @Test
    void getEnrolledStudents_returnsList() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g60");
        session.setEnrolledStudentIds(List.of("stu1", "stu2"));
        when(classSessionService.searchSessionById("g60")).thenReturn(session);

        mockMvc.perform(get("/api/groups/g60/students")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("stu1"))
                .andExpect(jsonPath("$[1]").value("stu2"));
    }

    @Test
    void getMostRequestedGroupChanges_returnsOk() throws Exception {
        Petition pet1 = new Petition(); pet1.setSubjectShortName("MAT"); pet1.setState(PetitionState.APPROVED); pet1.setStudentId("a");
        Petition pet2 = new Petition(); pet2.setSubjectShortName("MAT"); pet2.setState(PetitionState.REPROVED); pet2.setStudentId("b");
        when(petitionService.searchPetitionByType(PetitionType.CHANGE_GROUP)).thenReturn(List.of(pet1, pet2));

        mockMvc.perform(get("/api/groups/reports/most-requested-changes")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalChangeRequests").value(2))
                .andExpect(jsonPath("$.topRequestedSubjects").isArray());
    }
    @Test
    void createSessionFromRequest_subjectNotFound_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Test");
        req.setSubjectId("NOEXIST");
        req.setMaxStudents(10);

        when(subjectService.searchSubjectById("NOEXIST")).thenReturn(null);

        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La materia con ID NOEXIST no existe"));
    }

    @Test
    void createSessionFromRequest_professorNotFound_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Test");
        req.setSubjectId("SUBJ-1");
        req.setProfessorCode("NOEXIST");
        req.setMaxStudents(10);

        Subject subj = new Subject();
        subj.setId("SUBJ-1");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(4);
        subj.setLevel(1);

        when(subjectService.searchSubjectById("SUBJ-1")).thenReturn(subj);
        when(professorService.searchProfessorByCode("NOEXIST")).thenReturn(null);

        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El profesor con código NOEXIST no existe"));
    }

    @Test
    void updateSessionFromRequest_subjectNotFound_returnsBadRequest() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Grupo Test");
        req.setSubjectId("NOEXIST");
        req.setMaxStudents(20);

        ClassSession session = new ClassSession();
        session.setId("g1");

        when(classSessionService.searchSessionById("g1")).thenReturn(session);
        when(subjectService.searchSubjectById("NOEXIST")).thenReturn(null);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La materia con ID NOEXIST no existe"));
    }

    @Test
    void getWaitingList_groupNotFound_returnsNotFound() throws Exception {
        when(classSessionService.searchSessionById("g77")).thenReturn(null);
        mockMvc.perform(get("/api/groups/g77/waiting-list")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWaitingList_empty_returnsEmptyList() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g77");
        session.setWaitingListStudentIds(null); // o List.of()
        when(classSessionService.searchSessionById("g77")).thenReturn(session);

        mockMvc.perform(get("/api/groups/g77/waiting-list")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    @Test
    void deleteProfessor_profNotFound_returnsNotFound() throws Exception {
        when(professorService.searchProfessorByCode("P404")).thenReturn(null);
        mockMvc.perform(delete("/api/groups/professors/code/P404")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }
    @Test
    void getGroupsBySubjectReport_returnsMap() throws Exception {
        ClassSession cs = new ClassSession(); cs.setId("g1"); cs.setSubjectShortName("MAT");
        when(classSessionService.searchAllSessions()).thenReturn(List.of(cs));
        mockMvc.perform(get("/api/groups/reports/by-subject")
                        .session(deanSession()))
                .andExpect(status().isOk());
    }
    @Test
    void getAllProfessors_empty_returnsOk() throws Exception {
        when(professorService.searchAllProfessors()).thenReturn(List.of());
        mockMvc.perform(get("/api/groups/professors")
                        .session(deanSession()))
                .andExpect(status().isOk());
    }
    @Test
    void getProfessorByCode_notFound_returnsNotFound() throws Exception {
        when(professorService.searchProfessorByCode("P404")).thenReturn(null);
        mockMvc.perform(get("/api/groups/professors/code/P404")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }
    @Test
    void getAllSubjects_empty_returnsOk() throws Exception {
        when(subjectService.searchAllSubjects()).thenReturn(List.of());
        mockMvc.perform(get("/api/groups/subjects")
                        .session(deanSession()))
                .andExpect(status().isOk());
    }
    @Test
    void getSubjectById_subjectNotFound_returnsNotFound() throws Exception {
        when(subjectService.searchSubjectById("S404")).thenReturn(null);
        mockMvc.perform(get("/api/groups/subjects/S404")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }
    @Test
    void updateGlobalSchedule_groupNotFound_returnsNotFound() throws Exception {
        GroupsRequestDTO.GlobalScheduleRequest req = new GroupsRequestDTO.GlobalScheduleRequest();
        req.setDays(List.of("LUNES"));
        req.setStartTime("09:00");
        req.setEndTime("11:00");
        req.setClassroom("A1");

        when(classSessionService.searchSessionById("g100")).thenReturn(null);

        mockMvc.perform(put("/api/groups/g100/schedule/global")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGlobalSchedule_noMatchingDays_returnsBadRequest() throws Exception {
        GroupsRequestDTO.GlobalScheduleRequest req = new GroupsRequestDTO.GlobalScheduleRequest();
        req.setDays(List.of("LUNES"));
        req.setStartTime("09:00");
        req.setEndTime("11:00");
        req.setClassroom("A1");

        ClassSession session = new ClassSession();
        session.setId("g1");
        // Sin schedules para actualizar
        session.setSchedules(List.of());

        when(classSessionService.searchSessionById("g1")).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1/schedule/global")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateGlobalSchedule_validRequest_returnsOk() throws Exception {
        GroupsRequestDTO.GlobalScheduleRequest req = new GroupsRequestDTO.GlobalScheduleRequest();
        req.setDays(List.of("MARTES"));
        req.setStartTime("08:00");
        req.setEndTime("10:00");
        req.setClassroom("A2");

        ClassSchedule sched = new ClassSchedule();
        sched.setDayOfWeek("MARTES");
        sched.setStartTime(LocalTime.of(7, 0));
        sched.setEndTime(LocalTime.of(9, 0));
        sched.setClassroom("A1");

        ClassSession session = new ClassSession();
        session.setId("g2");
        session.setSchedules(List.of(sched));
        when(classSessionService.searchSessionById("g2")).thenReturn(session);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(put("/api/groups/g2/schedule/global")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void createSessionFromRequest_valid_createsSuccessfully() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Test");
        req.setSubjectId("SUBJ-1");
        req.setProfessorCode("PROF-1");
        req.setMaxStudents(10);

        Subject subj = new Subject();
        subj.setId("SUBJ-1");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(4);
        subj.setLevel(1);

        Professor prof = new Professor();
        prof.setProfessorCode("PROF-1");
        prof.setName("Profesor");

        when(subjectService.searchSubjectById("SUBJ-1")).thenReturn(subj);
        when(professorService.searchProfessorByCode("PROF-1")).thenReturn(prof);

        ClassSession session = new ClassSession();
        session.setId("g1");
        session.setGroupName("Test");
        when(classSessionService.createClassSession(any())).thenReturn(session);

        mockMvc.perform(post("/api/groups")
                        .session(deanSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupName").value("Test"));
    }
    @Test
    void getGroupsNearCapacity_noGroups_returnsEmptyList() throws Exception {
        when(classSessionService.searchAllSessions()).thenReturn(List.of());
        mockMvc.perform(get("/api/groups/reports/near-capacity")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getGroupsNearCapacity_someGroups_returnsList() throws Exception {
        ClassSession g1 = new ClassSession(); g1.setId("g1"); g1.setCapacity(10); g1.setEnrolledStudents(9);
        ClassSession g2 = new ClassSession(); g2.setId("g2"); g2.setCapacity(20); g2.setEnrolledStudents(18);

        when(classSessionService.searchAllSessions()).thenReturn(List.of(g1, g2));
        mockMvc.perform(get("/api/groups/reports/near-capacity")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    @Test
    void generateBasicDetailedStatistics_nullList_returnsZerosAndEmpties() throws Exception {
        Method m = GroupsController.class.getDeclaredMethod("generateBasicDetailedStatistics", List.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) m.invoke(
                new GroupsController(classSessionService, subjectService, professorService, petitionService),
                (Object) null
        );
        assertEquals(0, stats.get("totalRequests"));
        assertEquals(Map.of(), stats.get("subjectStateAnalysis"));
        assertEquals(List.of(), stats.get("mostActiveStudents"));
        assertEquals(0.0, stats.get("overallSuccessRate"));
        assertNotNull(stats.get("generatedAt"));
    }

    @Test
    void generateBasicDetailedStatistics_emptyList_returnsZerosAndEmpties() throws Exception {
        Method m = GroupsController.class.getDeclaredMethod("generateBasicDetailedStatistics", List.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) m.invoke(
                new GroupsController(classSessionService, subjectService, professorService, petitionService),
                (Object) null
        );

        assertEquals(0, stats.get("totalRequests"));
        assertEquals(Map.of(), stats.get("subjectStateAnalysis"));
        assertEquals(List.of(), stats.get("mostActiveStudents"));
        assertEquals(0.0, stats.get("overallSuccessRate"));
        assertNotNull(stats.get("generatedAt"));
    }

    @Test
    void generateBasicDetailedStatistics_singleApprovedRequest() throws Exception {
        Petition p = new Petition();
        p.setSubjectShortName("MAT");
        p.setState(PetitionState.APPROVED);
        p.setStudentId("stu1");

        Method m = GroupsController.class.getDeclaredMethod("generateBasicDetailedStatistics", List.class);
        m.setAccessible(true);
        GroupsController controller = new GroupsController(classSessionService, subjectService, professorService, petitionService);
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) m.invoke(controller, List.of(p));

        assertEquals(1, stats.get("totalRequests"));
        Map<String, Map<String, Long>> analysis = (Map<String, Map<String, Long>>) stats.get("subjectStateAnalysis");
        assertEquals(1L, analysis.get("MAT").get("APPROVED")); // <-- aquí debe estar el valor
    }
    @Test
    void getGroupsByClassroom_classroomWithResults_returnsList() throws Exception {
        ClassSchedule sched1 = new ClassSchedule();
        sched1.setClassroom("A101");
        sched1.setDayOfWeek("LUNES");
        sched1.setStartTime(LocalTime.of(8, 0));
        sched1.setEndTime(LocalTime.of(10, 0));
        ClassSession session1 = new ClassSession();
        session1.setId("g1");
        session1.setGroupName("Grupo A");
        session1.setSchedules(List.of(sched1));

        // Otro grupo en otro salón
        ClassSchedule sched2 = new ClassSchedule();
        sched2.setClassroom("B202");
        sched2.setDayOfWeek("MARTES");
        sched2.setStartTime(LocalTime.of(11, 0));
        sched2.setEndTime(LocalTime.of(13, 0));
        ClassSession session2 = new ClassSession();
        session2.setId("g2");
        session2.setGroupName("Grupo B");
        session2.setSchedules(List.of(sched2));

        when(classSessionService.searchAllSessions()).thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/groups/search/by-classroom")
                        .param("classroom", "A101")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value("g1"))
                .andExpect(jsonPath("$[0].groupName").value("Grupo A"))
                .andExpect(jsonPath("$[0].classroom").value("A101"))
                .andExpect(jsonPath("$[0].dayOfWeek").value("LUNES"))
                .andExpect(jsonPath("$[0].startTime").value("08:00"))
                .andExpect(jsonPath("$[0].endTime").value("10:00"))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void getGroupsByClassroom_noGroupsInClassroom_returnsEmptyList() throws Exception {
        ClassSchedule sched = new ClassSchedule();
        sched.setClassroom("B202");
        sched.setDayOfWeek("MARTES");
        sched.setStartTime(LocalTime.of(11, 0));
        sched.setEndTime(LocalTime.of(13, 0));
        ClassSession session = new ClassSession();
        session.setId("g2");
        session.setGroupName("Grupo B");
        session.setSchedules(List.of(sched));

        when(classSessionService.searchAllSessions()).thenReturn(List.of(session));

        mockMvc.perform(get("/api/groups/search/by-classroom")
                        .param("classroom", "A101")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }
    @Test
    void updateSessionFromRequest_subjectNotFound_throwsException() throws Exception {
        ClassSession session = new ClassSession();
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setSubjectId("NOEXIST");

        when(subjectService.searchSubjectById("NOEXIST")).thenReturn(null);

        Method m = GroupsController.class.getDeclaredMethod("updateSessionFromRequest", ClassSession.class, GroupsRequestDTO.class);
        m.setAccessible(true);
        GroupsController controller = new GroupsController(classSessionService, subjectService, professorService, petitionService);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> {
            m.invoke(controller, session, req);
        });

        Throwable cause = ex.getCause();
        assertTrue(cause instanceof IllegalArgumentException);
        assertEquals("La materia con ID NOEXIST no existe", cause.getMessage());
    }
    @Test
    void updateSessionFromRequest_updatesAllFields() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setGroupName("Nuevo Grupo");
        req.setDescription("Desc");
        req.setMaxStudents(50);
        req.setProfessorCode("PROF-1");
        req.setSubjectId("SUBJ-1");

        Professor prof = new Professor();
        prof.setProfessorCode("PROF-1");
        prof.setName("Prof Uno");
        prof.setMail("uno@uni.edu");
        prof.setDocument("1234567890");

        Subject subj = new Subject();
        subj.setId("SUBJ-1");
        subj.setShortName("MAT");
        subj.setName("Matemáticas");
        subj.setCredits(4);
        subj.setLevel(1);

        when(professorService.searchProfessorByCode("PROF-1")).thenReturn(prof);
        when(subjectService.searchSubjectById("SUBJ-1")).thenReturn(subj);

        ClassSession session = new ClassSession();

        Method m = GroupsController.class.getDeclaredMethod("updateSessionFromRequest", ClassSession.class, GroupsRequestDTO.class);
        m.setAccessible(true);
        GroupsController controller = new GroupsController(classSessionService, subjectService, professorService, petitionService);
        m.invoke(controller, session, req);

        assertEquals("Nuevo Grupo", session.getGroupName());
        assertEquals("Desc", session.getDescription());
        assertEquals(50, session.getCapacity());
        assertEquals("PROF-1", session.getProfessorCode());
        assertEquals("Prof Uno", session.getProfessorName());
        assertEquals("uno@uni.edu", session.getProfessorEmail());
        assertEquals("1234567890", session.getProfessorDocument());
        assertEquals("SUBJ-1", session.getSubjectId());
        assertEquals("MAT", session.getSubjectShortName());
        assertEquals("Matemáticas", session.getSubjectName());
        assertEquals(4, session.getSubjectCredits());
        assertEquals(1, session.getSubjectLevel());
    }

    @Test
    void updateSessionFromRequest_professorNotFound_throwsException() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO();
        req.setProfessorCode("NOEXIST");

        when(professorService.searchProfessorByCode("NOEXIST")).thenReturn(null);

        ClassSession session = new ClassSession();

        Method m = GroupsController.class.getDeclaredMethod("updateSessionFromRequest", ClassSession.class, GroupsRequestDTO.class);
        m.setAccessible(true);
        GroupsController controller = new GroupsController(classSessionService, subjectService, professorService, petitionService);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> {
            m.invoke(controller, session, req);
        });
        Throwable cause = ex.getCause();
        assertTrue(cause instanceof IllegalArgumentException);
        assertTrue(cause.getMessage().contains("profesor"));
    }

    @Test
    void updateSessionFromRequest_nullFields_doNotChange() throws Exception {
        GroupsRequestDTO req = new GroupsRequestDTO(); // All null
        ClassSession session = new ClassSession();
        session.setGroupName("Original");
        session.setCapacity(12);

        Method m = GroupsController.class.getDeclaredMethod("updateSessionFromRequest", ClassSession.class, GroupsRequestDTO.class);
        m.setAccessible(true);
        GroupsController controller = new GroupsController(classSessionService, subjectService, professorService, petitionService);

        m.invoke(controller, session, req);

        assertEquals("Original", session.getGroupName());
        assertEquals(12, session.getCapacity());
    }
    @Test
    void removeProfessorFromGroup_professorMatches_removesSuccessfully() throws Exception {
        // Prepara el grupo existente con el profesor asignado
        ClassSession session = new ClassSession();
        session.setId("g1");
        session.setProfessorCode("PROF-1");
        session.setProfessorName("Prof Uno");
        session.setProfessorEmail("prof.uno@escuelaing.edu.co");
        session.setProfessorDocument("123456789");

        when(classSessionService.searchSessionById("g1")).thenReturn(session);
        when(classSessionService.updateClassSession(any())).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1/professor/remove")
                        .param("professorCode", "PROF-1")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value("g1"))
                .andExpect(jsonPath("$.professorCode").doesNotExist()); // El profe fue removido pipiipipipi
    }

    @Test
    void removeProfessorFromGroup_professorDoesNotMatch_returnsBadRequest() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g1");
        session.setProfessorCode("PROF-1");
        when(classSessionService.searchSessionById("g1")).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1/professor/remove")
                        .param("professorCode", "WRONG")
                        .session(deanSession()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removeProfessorFromGroup_groupNotFound_returnsNotFound() throws Exception {
        when(classSessionService.searchSessionById("g1")).thenReturn(null);

        mockMvc.perform(put("/api/groups/g1/professor/remove")
                        .param("professorCode", "PROF-1")
                        .session(deanSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeProfessorFromGroup_unauthorized_returnsForbidden() throws Exception {
        ClassSession session = new ClassSession();
        session.setId("g1");
        session.setProfessorCode("PROF-1");
        when(classSessionService.searchSessionById("g1")).thenReturn(session);

        mockMvc.perform(put("/api/groups/g1/professor/remove")
                        .param("professorCode", "PROF-1")
                        .session(profSession())) // Solo decano/vice pueden
                .andExpect(status().isForbidden());
    }@Test
    void getDetailedChangeRequestStatistics_authorized_returnsOk() throws Exception {

        Petition petition1 = new Petition();
        petition1.setType(PetitionType.CHANGE_GROUP);
        petition1.setState(PetitionState.APPROVED); // ¡Clave!
        petition1.setSubjectShortName("DOSW");      // Si tu endpoint lo agrupa así
        petition1.setStudentId("student1");

        List<Petition> petitions = List.of(petition1);
        when(petitionService.searchPetitionByType(PetitionType.CHANGE_GROUP)).thenReturn(petitions);

        mockMvc.perform(get("/api/groups/reports/change-request-statistics")
                        .session(deanSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(1))
                .andExpect(jsonPath("$.mostActiveStudents[0].studentId").value("student1"))
                .andExpect(jsonPath("$.overallSuccessRate").value(100.0));
    }
    @Test
    void getDetailedChangeRequestStatistics_unauthorized_throwsException() throws Exception {
        mockMvc.perform(get("/api/groups/reports/change-request-statistics")
                        .session(studentSession()))
                .andExpect(status().isForbidden()) // <--- CORREGIDO
                .andExpect(jsonPath("$.message").value("No tienes permisos para ver estadísticas detalladas"));
    }
}