package com.swasth.swasth.service;

import com.swasth.swasth.dto.AuthRequest;
import com.swasth.swasth.dto.AuthResponse;
import com.swasth.swasth.entities.Patient;
import com.swasth.swasth.entities.User;
import com.swasth.swasth.repositories.PatientRepository;
import com.swasth.swasth.repositories.UserRepository;
import com.swasth.swasth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PatientRepository patientRepository;
    private final JwtUtil jwtUtil;

    /* ---------- registration ---------- */
    public AuthResponse register(AuthRequest dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        userRepository.save(user);

        Patient p = Patient.builder()
                .fullName(dto.getEmail())
                .accountHolder(user)
                .build();
        patientRepository.save(p);

        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(details))
                .refreshToken(jwtUtil.generateRefreshToken(details))
                .build();
    }

    /* ---------- login ---------- */
    public AuthResponse login(AuthRequest dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserDetails details = userDetailsService.loadUserByUsername(dto.getEmail());
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(details))
                .refreshToken(jwtUtil.generateRefreshToken(details))
                .build();
    }
}