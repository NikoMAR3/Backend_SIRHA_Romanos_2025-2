package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents an academic period in the institution.
 *
 * Stored in the "periods" collection in MongoDB.
 *
 * Fields:
 * - startDate: when the period begins.
 * - endDate: when the period ends.
 * - enabled: whether the period is currently active.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "periods")
public class Period {

    @Id
    private String id;

    private String startDate;
    private String endDate;
    private Boolean enabled;

}
