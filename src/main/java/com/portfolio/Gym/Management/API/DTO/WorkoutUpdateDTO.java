package com.portfolio.Gym.Management.API.DTO;

public record WorkoutUpdateDTO(
        String name,
        String description,
        Integer duration
) {
    public WorkoutUpdateDTO(String name, String description, Integer duration) {
        this.name = name;
        this.description = description;
        this.duration = duration;
    }
}
