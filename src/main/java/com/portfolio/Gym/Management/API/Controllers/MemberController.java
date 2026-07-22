package com.portfolio.Gym.Management.API.Controllers;

import com.portfolio.Gym.Management.API.DTO.MemberRequestDTO;
import com.portfolio.Gym.Management.API.DTO.MemberResponseDTO;
import com.portfolio.Gym.Management.API.DTO.MemberUpdateDTO;
import com.portfolio.Gym.Management.API.Services.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    public final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponseDTO> create(@RequestBody MemberRequestDTO inputData){
        MemberResponseDTO memberResponseDTO = memberService.createMember(inputData);

        return ResponseEntity.status(HttpStatus.CREATED).body(memberResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> listAll(){
        List<MemberResponseDTO> memberReponseList = memberService.listAll();
        return ResponseEntity.ok(memberReponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> findById( @PathVariable Long id){
        MemberResponseDTO memberResponseDTO = memberService.findById(id);
        return ResponseEntity.ok(memberResponseDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> update( @PathVariable long id, @RequestBody MemberUpdateDTO updateData){
        MemberResponseDTO memberResponseDTO = memberService.update(id, updateData);
        return ResponseEntity.ok(memberResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
