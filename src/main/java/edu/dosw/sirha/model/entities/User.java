package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public abstract class User {
    @Id
    private String id;

    private String name;
    private String mail;
    private String document;

    @Field("user_type")
    private UserType type;

    private List<String> petitionIds;

    protected User(String id, String name, String mail, String document, UserType type) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.document = document;
        this.type = type;
        this.petitionIds = new ArrayList<>();
    }
}
