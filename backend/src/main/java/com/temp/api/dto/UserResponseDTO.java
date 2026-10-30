package com.temp.api.dto;

import com.temp.api.model.User;

public record UserResponseDTO(Long id, String nome, String email, String role) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}