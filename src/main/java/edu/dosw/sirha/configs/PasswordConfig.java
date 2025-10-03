package edu.dosw.sirha.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for password encoding.
 * Defines a {@link PasswordEncoder} bean that uses {@link BCryptPasswordEncoder}
 * to securely hash and verify user passwords within the application.
 */
@Configuration
public class PasswordConfig {
    
    /**
     * Creates a {@link PasswordEncoder} bean using {@link BCryptPasswordEncoder}.
     * BCrypt is a strong hashing function designed for password storage, 
     * providing built-in salting and adaptive complexity.
     *
     * @return a {@link PasswordEncoder} instance that uses BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
