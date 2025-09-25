package edu.dosw.sirha.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para crear/actualizar grupo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequestDTO {

    @Schema(description = "Número del grupo", example = "1")
    @NotBlank(message = "El número del grupo es obligatorio")
    @Size(max = 2, message = "El número del grupo no puede exceder 2 caracteres")
    private String groupNumber;

    @Schema(description = "Código de la materia asociada", example = "DOSW")
    @NotBlank(message = "El código de la materia es obligatorio")
    @Size(max = 4, message = "El código no puede exceder 4 caracteres")
    private String subjectCode;

    @Schema(description = "Profesor asignado", example = "Juan Pérez")
    @NotBlank(message = "El profesor es obligatorio")
    @Size(max = 100, message = "El nombre del profesor no puede exceder 100 caracteres")
    private String professor;

    @Schema(description = "Aula asignada", example = "H110")
    @NotBlank(message = "El aula es obligatoria")
    @Size(max = 10, message = "El aula no puede exceder 10 caracteres")
    private String classroom;

    @Schema(description = "Capacidad máxima del grupo", example = "30")
    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede ser mayor a 100")
    private Integer maxCapacity;
}
