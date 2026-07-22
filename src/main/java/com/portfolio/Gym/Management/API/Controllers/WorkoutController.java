package com.portfolio.Gym.Management.API.Controllers;

import com.portfolio.Gym.Management.API.DTO.WorkoutRequestDTO;
import com.portfolio.Gym.Management.API.DTO.WorkoutResponseDTO;
import com.portfolio.Gym.Management.API.DTO.WorkoutUpdateDTO;
import com.portfolio.Gym.Management.API.Services.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }



//    POST
//    Receber JSON → Converter para RequestDTO → Chamar Service → Receber ResponseDTO →
//    Retornar HTTP 201.
    @PostMapping
    public ResponseEntity<WorkoutResponseDTO> createWorkout(@RequestBody WorkoutRequestDTO inputData){
        WorkoutResponseDTO workoutResponseDTO = workoutService.createWorkout(inputData);
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutResponseDTO);

    }
//    GET ALL
//    Receber requisição → Chamar Service → Receber lista → Retornar HTTP 200.
    @GetMapping
    public ResponseEntity<List<WorkoutResponseDTO>> listAll(){
        List<WorkoutResponseDTO> workoutResponseList = workoutService.listAll();
        return ResponseEntity.ok(workoutResponseList);
    }

//    GET BY ID
//    Receber ID da URL → Chamar Service → Receber ResponseDTO → Retornar HTTP 200.
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> findById(@PathVariable Long id){
        WorkoutResponseDTO responseDTO = workoutService.findById(id);
        return ResponseEntity.ok(responseDTO);
    }

//    PUT
//    Receber ID e JSON → Chamar Service → Receber objeto atualizado → Retornar HTTP 200.
@PutMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> update(@PathVariable Long id, @RequestBody WorkoutUpdateDTO updateData){
        WorkoutResponseDTO workoutResponseDTO = workoutService.update(id, updateData);
        return ResponseEntity.ok(workoutResponseDTO);
    }

//    DELETE
//    Receber ID → Chamar Service → Excluir → Retornar HTTP 204.
@DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
