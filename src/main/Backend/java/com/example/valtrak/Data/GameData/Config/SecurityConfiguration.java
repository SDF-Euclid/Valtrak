package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.GameData.Service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
    public static PasswordEncoder passwordEncoder() {return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();}

    /**
     * Guests can use the open endpoints (such as the card catalog). Account
     * endpoints need a signed-in player, identified by a bearer token.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AccountService accounts) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // token-based API, no cookies
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new TokenAuthenticationFilter(accounts), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/account/me", "/account/me/**", "/account/logout", "/game/**", "/decks", "/decks/**").authenticated()
                        .anyRequest().permitAll() // card catalog, nations and sign-in stay open to guests
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );
        return http.build();
    }
}
