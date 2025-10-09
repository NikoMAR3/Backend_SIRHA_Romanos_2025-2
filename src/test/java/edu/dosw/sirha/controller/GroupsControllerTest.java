package edu.dosw.sirha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.controller.dtos.GroupsRequestDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}