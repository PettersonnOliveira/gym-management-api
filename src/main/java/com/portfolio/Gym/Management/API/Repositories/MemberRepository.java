package com.portfolio.Gym.Management.API.Repositories;

import com.portfolio.Gym.Management.API.Entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
