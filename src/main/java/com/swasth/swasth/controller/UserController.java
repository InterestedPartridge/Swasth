package com.swasth.swasth.controller;

import com.swasth.swasth.dto.UserResponse;
import com.swasth.swasth.dto.UserRequest;
import com.swasth.swasth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.swasth.swasth.service.UserService;
import com.swasth.swasth.dto.AuthRequest;
import com.swasth.swasth.dto.AuthResponse;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public UserResponse create(@RequestBody UserRequest req) {
//        return userService.createUser(req);
//    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequest dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest dto) {
        return authService.login(dto);
    }


}
