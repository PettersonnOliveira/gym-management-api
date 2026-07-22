package com.portfolio.Gym.Management.API.Services;

import com.portfolio.Gym.Management.API.DTO.EnrollmentRequestDTO;
import com.portfolio.Gym.Management.API.DTO.EnrollmentResponseDTO;
import com.portfolio.Gym.Management.API.Entities.Enrollment;
import com.portfolio.Gym.Management.API.Entities.Member;
import com.portfolio.Gym.Management.API.Entities.Workout;
import com.portfolio.Gym.Management.API.Exceptions.ResourceNotFoundException;
import com.portfolio.Gym.Management.API.Repositories.EnrollmentRepository;
import com.portfolio.Gym.Management.API.Repositories.MemberRepository;
import com.portfolio.Gym.Management.API.Repositories.WorkoutRepository;
import com.portfolio.Gym.Management.API.Exceptions.BusinessRuleException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final WorkoutRepository workoutRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, MemberRepository memberRepository, WorkoutRepository workoutRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.memberRepository = memberRepository;
        this.workoutRepository = workoutRepository;
    }

    //    CREATE
//    Receber RequestDTO → Criar Entity → Salvar no banco → Receber Entity salva → Converter para
//    ResponseDTO → Retornar.
    public EnrollmentResponseDTO create(EnrollmentRequestDTO inputData) {
        Member member = memberRepository.findById(inputData.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("member not find"));
        Workout workout = workoutRepository.findById(inputData.workoutId())
                .orElseThrow(() -> new ResourceNotFoundException("workout not find"));

        if (enrollmentRepository.existsByMemberAndWorkout(member,workout)){
            throw new BusinessRuleException("matricula ja realizada");
        }

        Enrollment enrollment = new Enrollment(member,workout);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        Enrollment enrollmentSalva = enrollmentRepository.save(enrollment);
        return new EnrollmentResponseDTO(enrollmentSalva);
    }

    //            LIST ALL
//    Buscar todos os registros → Receber List<Entity> → Converter cada Entity para ResponseDTO →
//    Retornar lista.
    public List<EnrollmentResponseDTO> listAll() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return enrollments.stream()
                .map(EnrollmentResponseDTO::new)
                .toList();
    }

//    FIND BY ID
//    Receber ID → Buscar no banco → Se não existir, lançar exceção → Converter para ResponseDTO
//→ Retornar.
    public EnrollmentResponseDTO findById(Long id){
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollement não encontrado"));
       return  new EnrollmentResponseDTO(enrollment);
    }

//            DELETE
//    Receber ID → Buscar entidade → Excluir → Finalizar

    public void delete(Long id){
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment não encontrado"));
        enrollmentRepository.delete(enrollment);
    }
}
