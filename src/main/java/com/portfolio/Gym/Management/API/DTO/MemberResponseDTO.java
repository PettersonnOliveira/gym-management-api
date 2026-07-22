package com.portfolio.Gym.Management.API.DTO;

import com.portfolio.Gym.Management.API.Entities.Member;

public record MemberResponseDTO(
        Long id,
        String name,
        String email,
        String phone
) {
    public MemberResponseDTO(Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getPhone());
    }
}
