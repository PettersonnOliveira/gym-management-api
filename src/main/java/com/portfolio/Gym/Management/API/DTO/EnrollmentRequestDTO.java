package com.portfolio.Gym.Management.API.DTO;

import jakarta.validation.constraints.NotNull;


public record EnrollmentRequestDTO(
        @NotNull Long memberId,
        @NotNull Long workoutId

) {
}
