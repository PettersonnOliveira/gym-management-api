package com.portfolio.Gym.Management.API.Repositories;

import com.portfolio.Gym.Management.API.Entities.Enrollment;
import com.portfolio.Gym.Management.API.Entities.Member;
import com.portfolio.Gym.Management.API.Entities.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByMemberAndWorkout(Member member, Workout workout);
}
