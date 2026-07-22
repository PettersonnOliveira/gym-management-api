package com.portfolio.Gym.Management.API.DTO;

import com.portfolio.Gym.Management.API.Entities.Enrollment;

import java.time.LocalDateTime;

public record EnrollmentResponseDTO(
        Long id,
        LocalDateTime enrollmentDate,
        MemberResponseDTO member,
        WorkoutResponseDTO workout
) {
    public EnrollmentResponseDTO(Enrollment enrollment){
        this(enrollment.getId(), enrollment.getEnrollmentDate(),new MemberResponseDTO(enrollment.getMember()), new WorkoutResponseDTO(enrollment.getWorkout()));
    }
}
