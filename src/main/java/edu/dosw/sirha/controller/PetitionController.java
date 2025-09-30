package edu.dosw.sirha.controller;

import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.services.PetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/petitions")
@RequiredArgsConstructor
@Tag(name = "Petitions", description = "Endpoints to manage academic petitions")
public class PetitionController {
    private final PetitionService petitionService;

    //@PostMapping
    //@Operation(summary = "Create petition", description = "Creates a new petition and processes it through the chain of responsibility")
    //public ResponseEntity<Petition> createPetition(@RequestBody Petition petition) {
        //Petition saved = petitionService.createPetition(petition);
        //return ResponseEntity.ok(saved);
    //}
}
