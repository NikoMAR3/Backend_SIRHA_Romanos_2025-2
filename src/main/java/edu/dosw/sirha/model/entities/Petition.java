package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import java.time.LocalDateTime;
import java.util.List;


/**
 * Entity that represents a generated petition
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "petitions")
public class Petition {

    @Id
    private String petitionId;

    @Field
    private String subjectId;

    @Field
    private String studentId;

    @Field
    private String subjectShortName;

    @Field
    private String groupId;

    @Field
    private String subjectName;

    @Field
    private PetitionState state;

    @Field
    private PetitionType type;

    @Field
    private PetitionPriority priority;

    @Field
    private LocalDateTime creationDate;

    @Field
    private LocalDateTime modificationDate;

    @Field
    private String justification;

    @Field
    private String assignedReviewer;

    @Field
    private String rejectionReason;

    @Field
    private List<String> decisionHistory;

    @Field
    private String associateDeanery;

    @Field
    private Boolean isExceptionalCase = false;

    @Field
    private String exceptionalCaseJustification;

}
