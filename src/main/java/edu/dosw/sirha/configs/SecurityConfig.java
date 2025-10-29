package edu.dosw.sirha.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the SIRHA application.
 * Configures access permissions for different endpoints including public access to Swagger UI.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     * Allows public access to Swagger UI and API documentation while protecting other endpoints.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz

                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html", 
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                    //"/api/students/**"
                ).permitAll()


                .requestMatchers("/api/**").permitAll()
                
                
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
               
                .anyRequest().authenticated()
            )
            
            .csrf(csrf -> csrf.disable())
            
            
            .httpBasic(httpBasic -> httpBasic.realmName("SIRHA API"));

        return http.build();
    }
}