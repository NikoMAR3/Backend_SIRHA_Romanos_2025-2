/* 
package edu.dosw.sirha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.controller.dtos.StudentsRequestDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterStudentAsDean() throws Exception {
        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("Juan Perez");
        request.setMail("juan.perez@mail.escuelaing.edu.co");
        request.setDocument("1234567890");
        request.setStudentCode("20250001");
        request.setSemester(1);

        Dean deanUser = new Dean();
        deanUser.setId("dean123");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        mockMvc.perform(post("/api/students/register")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegisterAndUpdateStudentAsDean() throws Exception {
        String studentCode = "20250001"; // Usado como identificador único

        StudentsRequestDTO request = new StudentsRequestDTO();
        request.setName("Juan Perez");
        request.setMail("juan.perez@mail.escuelaing.edu.co");
        request.setDocument("1234567890");
        request.setStudentCode(studentCode);
        request.setSemester(1);

        Dean deanUser = new Dean();
        deanUser.setId("dean123");
        deanUser.setType(UserType.DEAN);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", deanUser);

        // Registrar el estudiante
        mockMvc.perform(post("/api/students/register")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Actualizar el estudiante usando el mismo studentCode
        StudentsRequestDTO updateRequest = new StudentsRequestDTO();
        updateRequest.setName("Juan Actualizado");
        updateRequest.setMail("juan.actualizado@mail.escuelaing.edu.co");
        updateRequest.setDocument("1234567890");
        updateRequest.setStudentCode(studentCode);
        updateRequest.setSemester(2);

        mockMvc.perform(put("/api/students/{studentId}", studentCode)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }
}
    
*/