package edu.dosw.sirha.configs;

import edu.dosw.sirha.model.components.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PetitionHandlerConfig {
    private final ProfessorHandler professorHandler;
    private final AcademicVicePresidentHandler viceHandler;
    private final DeanHandler deanHandler;

    @Bean
    public PetitionHandler petitionHandlerChain() {
        professorHandler.setNextHandler(viceHandler);
        viceHandler.setNextHandler(deanHandler);
        return professorHandler;
    }
}
