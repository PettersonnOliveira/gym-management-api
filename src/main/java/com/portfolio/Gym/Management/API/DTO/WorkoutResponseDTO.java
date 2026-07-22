package com.portfolio.Gym.Management.API.DTO;

import com.portfolio.Gym.Management.API.Entities.Workout;

public record WorkoutResponseDTO(
        Long id,
        String name,
        String description,
        Integer duration

) {

    public WorkoutResponseDTO(Workout workout) {
        this(workout.getId(), workout.getName(), workout.getDescription(), workout.getDuration());
    }
}
