package com.temp.api.dto;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponseDTO usuario
) {
}