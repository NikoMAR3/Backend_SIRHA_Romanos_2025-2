package edu.dosw.sirha.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para registro de estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequestDTO {

    @Schema(description = "Código estudiantil", example = "1000098136")
    @NotBlank(message = "El código estudiantil es obligatorio")
    @Size(min = 10, max = 10, message = "El código debe tener 10 caracteres")
    private String studentCode;

    @Schema(description = "Nombre completo", example = "Juan Pérez")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String fullName;

    @Schema(description = "Email institucional", example = "juan.perez@mail.escuelaing.edu.co")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;

    @Schema(description = "Contraseña", example = "password123")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Schema(description = "Carrera", example = "Ingeniería de Sistemas")
    @NotBlank(message = "La carrera es obligatoria")
    private String career;

    @Schema(description = "Semestre actual", example = "8")
    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe ser mayor a 0")
    @Max(value = 10, message = "El semestre no puede ser mayor a 10")
    private Integer currentSemester;
}
