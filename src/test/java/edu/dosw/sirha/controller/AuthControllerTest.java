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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AuthControllerTest{

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

    @Test
    void testLogout_Success() throws Exception {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("st01");
        loginRequest.setPassword("12345");

        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        // Usa la MISMA sesión en el logout
        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    void testGetCurrentUser_Success() throws Exception {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("st01");
        loginRequest.setPassword("12345");

        // Crea una sesión compartida
        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        // Usa la MISMA sesión en el GET
        mockMvc.perform(get("/api/auth/current-user")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("st01"));
    }

    @Test
    void testGetCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/current-user"))
                .andExpect(status().isUnauthorized());
    }

    // --- CHECK PERMISSION ---
    @Test
    void testCheckPermission_Success() throws Exception {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("st01");
        loginRequest.setPassword("12345");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        mockMvc.perform(get("/api/auth/check-permission/anyResource")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resource").value("anyResource"));
    }

    @Test
    void testCheckPermission_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/check-permission/anyResource"))
                .andExpect(status().isUnauthorized());
    }

    // --- SET PASSWORD ---
    @Test
    void testSetPassword_FailsForOwnAccount() throws Exception {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("st01");
        loginRequest.setPassword("12345");

        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        AuthDto.SetPasswordRequest req = new AuthDto.SetPasswordRequest();
        req.setUserId("st01");
        req.setNewPassword("newpass");

        mockMvc.perform(post("/api/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // --- REGISTER USER ---
    @Test
    void testRegisterUser_Success() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setId("st02");
        req.setName("Maria");
        req.setMail("maria@example.com");
        req.setType(UserType.STUDENT);
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRegisterUser_UnsupportedType() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setId("st03");
        req.setName("Nope");
        req.setMail("nope@example.com");
        // Pasa un tipo inválido si tienes un enum que no está soportado
        req.setType(null);
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("must not be null"));
    }

    // --- GET ALL USERS ---
    @Test
    void testGetAllUsers_AsAdmin() throws Exception {
        // Registra y loguea un admin (academic vice)
        Student admin = new Student();
        admin.setId("admin01");
        admin.setName("Admin");
        admin.setMail("admin@example.com");
        admin.setType(UserType.ACADEMIC_VICEPRESIDENT);
        admin.setActive(true);
        authenticationService.registerUser(admin, "adminpass");

        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("admin01");
        loginRequest.setPassword("adminpass");

        // Crea una sesión que vas a compartir
        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        // Usa la MISMA sesión para el request protegido
        mockMvc.perform(get("/api/auth/users")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllUsers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isUnauthorized());
    }

    // --- TOGGLE USER STATUS ---
    @Test
    void testToggleUserStatus_FailsForOwnAccount() throws Exception {
        // Registra y loguea un admin (academic vice)
        Student admin = new Student();
        admin.setId("admin01");
        admin.setName("Admin");
        admin.setMail("admin@example.com");
        admin.setType(UserType.ACADEMIC_VICEPRESIDENT);
        admin.setActive(true);
        authenticationService.registerUser(admin, "adminpass");

        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("admin01");
        loginRequest.setPassword("adminpass");

        MockHttpSession session = new MockHttpSession(); // <-- crea una sesión compartida

        // Login usando esa sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        // Usa la misma sesión en el PUT
        mockMvc.perform(put("/api/auth/users/admin01/toggle-status")
                        .session(session))
                .andExpect(status().isBadRequest()) // Ahora sí, espera el 400 de tu lógica
                .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    void testSetPassword_Unauthenticated() throws Exception {
        AuthDto.SetPasswordRequest req = new AuthDto.SetPasswordRequest();
        req.setUserId("someone");
        req.setNewPassword("newpass");

        mockMvc.perform(post("/api/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // SET PASSWORD - No privilegios (logueado como estudiante)
    @Test
    void testSetPassword_ForbiddenForNonAdmin() throws Exception {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("st01");
        loginRequest.setPassword("12345");

        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        AuthDto.SetPasswordRequest req = new AuthDto.SetPasswordRequest();
        req.setUserId("another");
        req.setNewPassword("newpass");

        // Usa la MISMA sesión en el POST
        mockMvc.perform(post("/api/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .session(session))
                .andExpect(status().isForbidden())   // Ahora sí, espera 403
                .andExpect(jsonPath("$.success").value(false));
    }

    // TOGGLE USER STATUS - Unauthenticated
    @Test
    void testToggleUserStatus_Unauthenticated() throws Exception {
        mockMvc.perform(put("/api/auth/users/anything/toggle-status"))
                .andExpect(status().isUnauthorized());
    }

    // TOGGLE USER STATUS - No privilegios (logueado como estudiante)

    @Test
    void testToggleUserStatus_ForbiddenForNonAdmin() throws Exception {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("st01");
        loginRequest.setPassword("12345");

        // Crea una sesión compartida
        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session)); // importante

        // Usa la MISMA sesión en el PUT
        mockMvc.perform(put("/api/auth/users/someone/toggle-status")
                        .session(session)) // importante
                .andExpect(status().isForbidden());
    }

    // TOGGLE USER STATUS - Usuario a cambiar no existe
    @Test
    void testToggleUserStatus_UserNotFound() throws Exception {
        // Registra y loguea un admin
        Student admin = new Student();
        admin.setId("admin01");
        admin.setName("Admin");
        admin.setMail("admin@example.com");
        admin.setType(UserType.ACADEMIC_VICEPRESIDENT);
        admin.setActive(true);
        authenticationService.registerUser(admin, "adminpass");

        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("admin01");
        loginRequest.setPassword("adminpass");

        MockHttpSession session = new MockHttpSession();

        // Login usando la sesión compartida
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        // Usa la MISMA sesión en el PUT
        mockMvc.perform(put("/api/auth/users/noexiste/toggle-status")
                        .session(session))
                .andExpect(status().isNotFound());
    }

    // REGISTER USER - Datos inválidos (por ejemplo, mail vacío)
    @Test
    void testRegisterUser_BadRequestForInvalidData() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setId("st04");
        req.setName("SinMail");
        req.setMail(""); // Inválido
        req.setType(UserType.STUDENT);
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testSetPassword_Success_AdminChangesOtherUser() throws Exception {
        // Registra y loguea un admin
        Student admin = new Student();
        admin.setId("admin01");
        admin.setName("Admin");
        admin.setMail("admin@example.com");
        admin.setType(UserType.ACADEMIC_VICEPRESIDENT);
        admin.setActive(true);
        authenticationService.registerUser(admin, "adminpass");

        // Agrega otro usuario
        Student user = new Student();
        user.setId("st02");
        user.setName("Maria");
        user.setMail("maria@example.com");
        user.setType(UserType.STUDENT);
        user.setActive(true);
        authenticationService.registerUser(user, "12345");

        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("admin01");
        loginRequest.setPassword("adminpass");
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        AuthDto.SetPasswordRequest req = new AuthDto.SetPasswordRequest();
        req.setUserId("st02");
        req.setNewPassword("nuevopass");

        mockMvc.perform(post("/api/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    void testToggleUserStatus_AdminTogglesUserStatus_Success() throws Exception {
        // Registra y loguea un admin
        Student admin = new Student();
        admin.setId("admin01");
        admin.setName("Admin");
        admin.setMail("admin@example.com");
        admin.setType(UserType.ACADEMIC_VICEPRESIDENT);
        admin.setActive(true);
        authenticationService.registerUser(admin, "adminpass");

        // Registra un estudiante
        Student student = new Student();
        student.setId("st02");
        student.setName("Maria");
        student.setMail("maria@example.com");
        student.setType(UserType.STUDENT);
        student.setActive(true);
        authenticationService.registerUser(student, "password");

        // Login admin y comparte sesión
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("admin01");
        loginRequest.setPassword("adminpass");
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        // Toggle status de st02 (de activo a inactivo)
        mockMvc.perform(put("/api/auth/users/st02/toggle-status")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Toggle status de st02 (de inactivo a activo)
        mockMvc.perform(put("/api/auth/users/st02/toggle-status")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    void testSetPassword_FailsForInvalidPassword() throws Exception {
        Student admin = new Student();
        admin.setId("admin01");
        admin.setName("Admin");
        admin.setMail("admin@example.com");
        admin.setType(UserType.ACADEMIC_VICEPRESIDENT);
        admin.setActive(true);
        authenticationService.registerUser(admin, "adminpass");

        // Registra otro estudiante
        Student student = new Student();
        student.setId("st02");
        student.setName("Maria");
        student.setMail("maria@example.com");
        student.setType(UserType.STUDENT);
        student.setActive(true);
        authenticationService.registerUser(student, "password");
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setId("admin01");
        loginRequest.setPassword("adminpass");
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .session(session));

        AuthDto.SetPasswordRequest req = new AuthDto.SetPasswordRequest();
        req.setUserId("st02");
        req.setNewPassword(""); // Vacío

        mockMvc.perform(post("/api/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .session(session))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testRegisterUser_FailsForDuplicateId() throws Exception {
        // Registra primero
        AuthDto.RegisterRequest req1 = new AuthDto.RegisterRequest();
        req1.setId("st02");
        req1.setName("Maria");
        req1.setMail("maria@example.com");
        req1.setType(UserType.STUDENT);
        req1.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Intenta registrar otra con mismo id
        AuthDto.RegisterRequest req2 = new AuthDto.RegisterRequest();
        req2.setId("st02");
        req2.setName("Otra Maria");
        req2.setMail("otra@example.com");
        req2.setType(UserType.STUDENT);
        req2.setPassword("password456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Resource"));
    }
    @Test
    void testLogin_FailsForNonexistentUser() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setId("idontexist");
        request.setPassword("cualquier");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Invalid credentials: User not found or inactive"));
    }
    @Test
    void testLogin_FailsForInactiveUser() throws Exception {
        Student student = new Student();
        student.setId("st02");
        student.setName("Maria");
        student.setMail("maria@example.com");
        student.setType(UserType.STUDENT);
        student.setActive(false);
        authenticationService.registerUser(student, "password");

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setId("st02");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Invalid credentials: User not found or inactive"));
    }
    @Test
    void testLogout_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
