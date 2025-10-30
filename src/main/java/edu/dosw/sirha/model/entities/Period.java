package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity that represents an academic period in the institution.
 *
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
