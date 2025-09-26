package edu.dosw.sirha.controller.dto;

import edu.dosw.sirha.model.PetitionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para crear solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetitionRequestDTO {

    @Schema(description = "Tipo de solicitud", example = "CHANGE")
    @NotNull(message = "El tipo de solicitud es obligatorio")
    private PetitionType petitionType;

    @Schema(description = "Codigo de la materia actual", example = "DOSW")
    @NotNull(message = "La materia actual es obligatoria")
    private Long currentSubjectCode;

    @Schema(description = "ID del grupo actual", example = "2")
    @NotNull(message = "El grupo actual es obligatorio")
    private Long currentGroupId;

    @Schema(description = "Codigo de la materia solicitada", example = "DOSW")
    private Long requestedSubjectCode;

    @Schema(description = "ID del grupo solicitado", example = "1")
    private Long requestedGroupId;

    @Schema(description = "Justificación de la solicitud")
    @NotBlank(message = "La justificación es obligatoria")
    @Size(max = 500, message = "La justificación no puede exceder 500 caracteres")
    private String justification;
}
