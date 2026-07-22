package com.portfolio.Gym.Management.API.DTO;

public record WorkoutRequestDTO(
        String name,
        String description,
        Integer duration
)  {
}
