/* 
package edu.dosw.sirha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
public class GroupsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Test
    void testCreateGroupAsDean() throws Exception {
    
        Subject subject = new Subject();
        subject.setId("ALLI");
        subject.setShortName("ALLI");
        subject.setName("Algoritmos");
        subjectRepository.save(subject);

    
        Professor professor = new Professor();
        professor.setId("1000098786");
        professor.setName("Juan Profesor");
        professor.setMail("juan.profesor@mail.com");
        professor.setDocument("123456789");
        professorRepository.save(professor);

    
        GroupsRequestDTO request = new GroupsRequestDTO();
        request.setGroupName("Grupo 1");
        request.setSubjectId("ALLI");
        request.setMaxStudents(30);
        request.setProfessorId("1000098786");

        Dean deanUser = new Dean();
        deanUser.setId("dean123");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


   @Test
    void testCreateSubjectAsDean() throws Exception {
        GroupsRequestDTO.SubjectRequest request = new GroupsRequestDTO.SubjectRequest();
        request.setSubjectId("FUPR");
        request.setSubjectShortName("FUPR");
        request.setSubjectName("Fundamentos de Programación");
        request.setSubjectCredits(3);
        request.setSubjectLevel(1);

        Dean deanUser = new Dean();
        deanUser.setId("dean123");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();

        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups/subjects")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateProfessorAsDean() throws Exception {
        GroupsRequestDTO.ProfessorRequest request = new GroupsRequestDTO.ProfessorRequest();
        request.setName("Juan Cs");
        request.setMail("juan.cors@mail.com");
        request.setDocument("1234874545");
        request.setProfessorCode("1000058786");

        Dean deanUser = new Dean();
        deanUser.setId("dean123");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups/professors")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


    @Test
    void testGetAllProfessors() throws Exception {
        
        GroupsRequestDTO.ProfessorRequest request = new GroupsRequestDTO.ProfessorRequest();
        request.setName("Juan Cs");
        request.setMail("juan.cors@mail.com");
        request.setDocument("233487443");
        request.setProfessorCode("233487443");

        Dean deanUser = new Dean();
        deanUser.setId("dn1245456");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups/professors")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        
        mockMvc.perform(get("/api/groups/professors")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].professorCode", hasItem("233487443")));
        }

    @Test
    void testGetProfessorByCode() throws Exception {
       
        GroupsRequestDTO.ProfessorRequest request = new GroupsRequestDTO.ProfessorRequest();
        request.setName("Juan s");
        request.setMail("juan.cs@mail.com");
        request.setDocument("7434948123");
        request.setProfessorCode("7434948123");

        Dean deanUser = new Dean();
        deanUser.setId("den123");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups/professors")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        
        mockMvc.perform(get("/api/groups/professors/code/{professorCode}", "7434948123")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professorCode").value("7434948123"));
    }

    @Test
    void testUpdateProfessorByCode() throws Exception {
        
        GroupsRequestDTO.ProfessorRequest createRequest = new GroupsRequestDTO.ProfessorRequest();
        createRequest.setName("Juan 123");
        createRequest.setMail("juan.corps@mail.com");
        createRequest.setDocument("1467874545");
        createRequest.setProfessorCode("1103458786");

        Dean deanUser = new Dean();
        deanUser.setId("dean12345");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups/professors")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        
        GroupsRequestDTO.ProfessorRequest updateRequest = new GroupsRequestDTO.ProfessorRequest();
        updateRequest.setName("pedro ActualizadoOO");
        updateRequest.setMail("pedro.actualizadoOO@mail.com");
        updateRequest.setDocument("1244567899");
        updateRequest.setProfessorCode("1330058786");

        mockMvc.perform(put("/api/groups/professors/code/{professorCode}", "1103458786")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("pedro ActualizadoOO"))
                .andExpect(jsonPath("$.professorCode").value("1330058786"));
    }

    @Test
    void testDeleteProfessorByCode() throws Exception {
        
        GroupsRequestDTO.ProfessorRequest request = new GroupsRequestDTO.ProfessorRequest();
        request.setName("Juan HOLA");
        request.setMail("juan.HOLrs@mail.com");
        request.setDocument("9776543211");
        request.setProfessorCode("9776543211");

        Dean deanUser = new Dean();
        deanUser.setId("de12345678");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/groups/professors")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


        mockMvc.perform(delete("/api/groups/professors/code/{professorCode}", "9776543211")
                .session(session))
                .andExpect(status().isNoContent());
    }

    
}
*/