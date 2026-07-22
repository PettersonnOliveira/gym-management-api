package com.portfolio.Gym.Management.API.Controllers;

import com.portfolio.Gym.Management.API.DTO.EnrollmentRequestDTO;
import com.portfolio.Gym.Management.API.DTO.EnrollmentResponseDTO;
import com.portfolio.Gym.Management.API.Repositories.EnrollmentRepository;
import com.portfolio.Gym.Management.API.Repositories.MemberRepository;
import com.portfolio.Gym.Management.API.Repositories.WorkoutRepository;
import com.portfolio.Gym.Management.API.Services.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDTO> createEnrolment (@RequestBody @Valid EnrollmentRequestDTO inputData){
        EnrollmentResponseDTO enrollmentRequestDTO = enrollmentService.create(inputData);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentRequestDTO);
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponseDTO>> listAll(){
        List<EnrollmentResponseDTO> enrollmentList = enrollmentService.listAll();
        return ResponseEntity.ok(enrollmentList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
