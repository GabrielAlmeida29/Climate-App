package com.temp.api.controller;

import com.temp.api.dto.AuthResponseDTO;
import com.temp.api.dto.LoginRequestDTO;
import com.temp.api.dto.RegisterRequestDTO;
import com.temp.api.dto.UserResponseDTO;
import com.temp.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO cadastrar(@Valid @RequestBody RegisterRequestDTO dto) {
        return authService.cadastrar(dto);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.autenticar(dto);
    }

    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal Jwt jwt) {
        return authService.buscarPerfil(Long.valueOf(jwt.getSubject()));
    }
}