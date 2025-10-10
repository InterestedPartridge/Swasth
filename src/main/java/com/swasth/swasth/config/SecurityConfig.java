package com.swasth.swasth.config;

import com.swasth.swasth.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;   // we’ll write it in step 3

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(fo -> fo.disable()))
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/**", "/family/join", "/h2-console/**",
                                "/users/**", "/swagger-ui.html/**", "/swagger-ui/**",
                                "/v3/api-docs/**", "/api-docs").permitAll()
                        .requestMatchers("/family/**").hasRole("USER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authMgr(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}