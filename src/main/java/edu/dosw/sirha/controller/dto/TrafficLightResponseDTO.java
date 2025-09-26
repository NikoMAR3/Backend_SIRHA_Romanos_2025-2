package edu.dosw.sirha.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "DTO para respuesta del semáforo del estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrafficLightResponseDTO {

    @Schema(description = "Id estudiantil", example = "1000098136")
    @NotBlank(message = "El id estudiantil es obligatorio")
    @Size(min = 10, max = 10, message = "El id debe tener 10 caracteres")
    private String studentId;

    @Schema(description = "Nombre completo", example = "Juan Pérez")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String fullName;

    @Schema(description = "Programa académico", example = "Ingeniería de sistemas")
    @NotBlank(message = "El programa académico es obligatorio")
    private String major;

    @Schema(description = "Asignaturas aprobadas")
    @NotNull(message = "La lista de asignaturas aprobadas no puede ser nula")
    private List<SubjectResponseDTO> subjectsApproved;

    @Schema(description = "Asignaturas canceladas")
    @NotNull(message = "La lista de asignaturas canceladas no puede ser nula")
    private List<SubjectResponseDTO> subjectsCanceled;

    @Schema(description = "Asignaturas perdidas")
    @NotNull(message = "La lista de asignaturas perdidas no puede ser nula")
    private List<SubjectResponseDTO> subjectsFailed;

    @Schema(description = "Asignaturas")
    @NotNull(message = "La lista de asignaturas no puede ser nula")
    private List<SubjectResponseDTO> subjects;

}
