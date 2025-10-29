package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.ReportType;
import lombok.Data;

import java.util.Date;

@Data
public class ReportRequestDTO {
    private ReportType reportType;
    private Date startDate;
    private Date endDate;
    private String deanery;
    private String subject;
    private String petitionState;
    private String classSession;
}
