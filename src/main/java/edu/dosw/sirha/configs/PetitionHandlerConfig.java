package edu.dosw.sirha.configs;

import edu.dosw.sirha.model.components.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up the chain of responsibility used to process petitions.
 */
@Configuration
@RequiredArgsConstructor
public class PetitionHandlerConfig {
    private final ProfessorHandler professorHandler;
    private final AcademicVicePresidentHandler viceHandler;
    private final DeanHandler deanHandler;

/**
 * Creates and configures the petition handler chain
 * @return the configured chain.
 */
 @Bean
    public PetitionHandler petitionHandlerChain() {
        professorHandler.setNextHandler(viceHandler);
        viceHandler.setNextHandler(deanHandler);
        return professorHandler;
    }
}
