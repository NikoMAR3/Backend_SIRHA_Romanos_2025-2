package edu.dosw.sirha.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Schema(description = "DTO de respuesta para materia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponseDTO {

    @Schema(description = "ID único de la materia")
    private Long id;

    @Schema(description = "Código de la materia", example = "DOSW")
    private String subjectCode;

    @Schema(description = "Grupo de la materia", example = "2")
    private String groupId;

    @Schema(description = "Nombre de la materia", example = "Desarrollo de operaciones Software")
    private String name;

    @Schema(description = "Número de créditos", example = "4")
    private Integer credits;

    @Schema(description = "Semestre recomendado", example = "6")
    private Integer recommendedSemester;

    @Schema(description = "Descripción de la materia", example = "Materia enfocada en el desarrollo de software utilizando metodologías ágiles.")
    private String description;

    @Schema(description = "Lista de grupos disponibles")
    private List<GroupResponseDTO> availableGroups;
}

