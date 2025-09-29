package edu.dosw.sirha.controller.dto;

import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "DTO de respuesta para solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetitionResponseDTO {

    @Schema(description = "ID único de la solicitud", example = "1")
    private Long id;

    @Schema(description = "Tipo de solicitud", example = "CHANGE")
    private PetitionType petitionType;

    @Schema(description = "Estado de la solicitud", example = "PENDIENTE")
    private PetitionState status;

    @Schema(description = "ID del estudiante que hizo la solicitud", example = "1000098136")
    private String studentId;

    @Schema(description = "Nombre del estudiante", example = "Juan Pérez")
    private String studentName;

    @Schema(description = "Codigo de la materia actual", example = "DOSW")
    private Long currentSubjectCode;

    @Schema(description = "ID del grupo actual", example = "2")
    private Long currentGroupId;

    @Schema(description = "Codigo de la materia solicitada", example = "DOSW")
    private Long requestedSubjectCode;

    @Schema(description = "ID del grupo solicitado", example = "1")
    private Long requestedGroupId;

    @Schema(description = "Justificación de la solicitud")
    private String justification;

    @Schema(description = "Fecha de creación de la solicitud")
    private LocalDateTime createdAt;

    @Schema(description = "Observaciones del decano o administrador")
    private String observations;
}
