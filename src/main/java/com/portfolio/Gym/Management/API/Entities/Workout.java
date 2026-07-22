package com.portfolio.Gym.Management.API.Entities;

import com.portfolio.Gym.Management.API.DTO.WorkoutRequestDTO;
import com.portfolio.Gym.Management.API.DTO.WorkoutUpdateDTO;
import jakarta.persistence.*;

import java.util.Objects;
@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private int duration; // in minutes

    public Workout() {
    }

    public Workout (WorkoutRequestDTO inputData) {
        this.name = inputData.name();
        this.description = inputData.description();
        this.duration = inputData.duration();
    }
    public Workout (WorkoutUpdateDTO updateData) {
        this.name = updateData.name();
        this.description = updateData.description();
        this.duration = updateData.duration();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Workout workout = (Workout) o;
        return Objects.equals(id, workout.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
