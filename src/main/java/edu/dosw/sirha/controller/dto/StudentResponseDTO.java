package edu.dosw.sirha.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Schema(description = "DTO de respuesta para estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDTO {

    @Schema(description = "ID único del estudiante", example = "1")
    private Long id;

    @Schema(description = "Código estudiantil", example = "1000098136")
    private String studentCode;

    @Schema(description = "Nombre completo", example = "Juan Pérez")
    private String fullName;

    @Schema(description = "Email institucional", example = "juan.perez@mail.escuelaing.edu.co")
    private String email;

    @Schema(description = "Carrera", example = "Ingeniería de Sistemas")
    private String career;

    @Schema(description = "Semestre actual", example = "8")
    private Integer currentSemester;

    @Schema(description = "Estado del semáforo académico", example = "VERDE")
    private String trafficLightStatus;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

}
