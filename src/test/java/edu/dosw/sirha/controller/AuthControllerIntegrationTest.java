package edu.dosw.sirha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.sirha.Application;
import edu.dosw.sirha.controller.dtos.AuthDto;
import edu.dosw.sirha.model.entities.Student;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.UserRepository;
import edu.dosw.sirha.model.services.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración real para AuthController con Mongo embebido.
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class // Desactiva seguridad para el test
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        Student student = new Student();
        student.setId("st01");
        student.setName("Juan Pérez");
        student.setMail("juan@example.com");
        student.setType(UserType.STUDENT);
        student.setActive(true);

        authenticationService.registerUser(student, "12345");
    }

    @Test
    void testLoginIntegration_Success() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setId("st01");
        request.setPassword("12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.id").value("st01"));
    }

    @Test
    void testLoginIntegration_FailsWithWrongPassword() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setId("st01");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
