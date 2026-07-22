package com.portfolio.Gym.Management.API.Services;

import com.portfolio.Gym.Management.API.DTO.MemberRequestDTO;
import com.portfolio.Gym.Management.API.DTO.MemberResponseDTO;
import com.portfolio.Gym.Management.API.DTO.MemberUpdateDTO;
import com.portfolio.Gym.Management.API.Entities.Member;
import com.portfolio.Gym.Management.API.Repositories.MemberRepository;
import org.springframework.stereotype.Service;
import com.portfolio.Gym.Management.API.Exceptions.ResourceNotFoundException;
import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponseDTO createMember(MemberRequestDTO inputData ){
        Member member = new Member(inputData);
        Member memberSalva = memberRepository.save(member);
        return new MemberResponseDTO(memberSalva);
    }

    public List<MemberResponseDTO> listAll(){
     List<Member> members = memberRepository.findAll();
     return members.stream()
             .map(MemberResponseDTO::new)
             .toList();
    }

    public MemberResponseDTO findById(Long id){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado"));
        return new MemberResponseDTO(member);
    }
    public MemberResponseDTO update(Long id, MemberUpdateDTO updateData){
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado"));

            member.setName(updateData.name());
            member.setEmail(updateData.email());
            member.setPhone(updateData.phone());

            Member memberUpdate = memberRepository.save(member);
            return new MemberResponseDTO(memberUpdate);
    }

    public void delete(Long id){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado"));
        memberRepository.delete(member);
    }
}


