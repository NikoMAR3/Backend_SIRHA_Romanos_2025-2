package edu.dosw.sirha.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Schema(description = "DTO de respuesta para grupo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDTO {

    @Schema(description = "ID único del grupo")
    private Long id;

    @Schema(description = "Número del grupo", example = "1")
    private String groupNumber;

    @Schema(description = "Código de la materia asociada", example = "DOSW")
    private String subjectCode;

    @Schema(description = "Nombre de la materia", example = "Desarrollo de operaciones Software")
    private String subjectName;

    @Schema(description = "Profesor asignado", example = "Juan Pérez")
    private String professor;

    @Schema(description = "Aula asignada", example = "H110")
    private String classroom;

    @Schema(description = "Capacidad máxima del grupo", example = "30")
    private Integer maxCapacity;

    @Schema(description = "Número de estudiantes inscritos", example = "25")
    private Integer currentEnrollment;

    @Schema(description = "Indica si el grupo tiene cupos disponibles")
    private Boolean hasAvailableSpots;
}
