package com.example.valtrak.Data.GameData.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The security configuration class for Valtrak. It configures Argon2's
 * password hashing, which is used throughout the backend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    /**
     * INIT: The constructor initializing the PasswordEncoder using the default
     * Argon2 salting and hashing methods. The @Bean annotation allows it to be
     * automatically injected into classes where password encoding is needed and
     * the settings can be manually changed in the future if need be.
     * @return The PasswordEncoder object used to hash passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for WebSocket connections
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all traffic as of now
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );
        return http.build();
    }
}