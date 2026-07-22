package com.portfolio.Gym.Management.API.Repositories;

import com.portfolio.Gym.Management.API.Entities.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
}
