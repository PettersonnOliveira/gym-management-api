package com.portfolio.Gym.Management.API.DTO;

public record AuthResponseDTO(
        String token,
        String email,
        String role
) {
}
