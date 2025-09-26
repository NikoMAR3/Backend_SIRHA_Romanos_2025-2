package edu.dosw.sirha.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Schema(description = "DTO para crear/actualizar materia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequestDTO {

    @Schema(description = "Código de la materia", example = "DOSW")
    @NotBlank(message = "El código de la materia es obligatorio")
    @Size(max = 4, message = "El código no puede exceder 4 caracteres")
    private String subjectCode;

    @Schema(description = "Grupo de la materia", example = "2")
    @NotBlank(message = "El grupo de la materia es obligatorio")
    @Size(max = 2, message = "El grupo no puede exceder 2 caracteres")
    private String groupId;

    @Schema(description = "Nombre de la materia", example = "Desarrollo de operaciones de software")
    @NotBlank(message = "El nombre de la materia es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String name;

    @Schema(description = "Número de créditos", example = "4")
    @NotNull(message = "Los créditos son obligatorios")
    @Min(value = 1, message = "Los créditos deben ser mayor a 0")
    @Max(value = 4, message = "Los créditos no pueden ser mayor a 4")
    private Integer credits;

    @Schema(description = "Semestre recomendado", example = "6")
    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe ser mayor a 0")
    private Integer recommendedSemester;

    @Schema(description = "Descripción de la materia")
    private String description;

}
