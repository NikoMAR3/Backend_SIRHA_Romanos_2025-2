package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "academicVicePresidents")

public class AcademicVicePresident extends User{

    public AcademicVicePresident(String name, String mail, String document) {
        super(name, mail, document, UserType.ACADEMIC_VICEPRESIDENT);
    }
}
