package com.portfolio.Gym.Management.API.DTO;

public record MemberUpdateDTO(
        String name,
        String email,
        String phone
) {
    public MemberUpdateDTO(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
