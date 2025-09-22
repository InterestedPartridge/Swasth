package com.swasth.swasth.service;

import com.swasth.swasth.dto.UserRequest;
import com.swasth.swasth.dto.UserResponse;
import com.swasth.swasth.entities.User;
import com.swasth.swasth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- import
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    public UserResponse createUser(UserRequest req) {
//        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
//            throw new IllegalArgumentException("Email already exists");
//        }
//
//        User user = User.builder()
//                .email(req.getEmail())
//                .password(passwordEncoder.encode(req.getPassword()))
//                .fullName(req.getFullName())
//                .build();
//
//        User saved = userRepository.save(user);
//        return new UserResponse(saved.getId(), saved.getEmail(), saved.getFullName());
//    }
}