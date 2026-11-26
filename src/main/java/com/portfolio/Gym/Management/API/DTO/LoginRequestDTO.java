package com.portfolio.Gym.Management.API.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
