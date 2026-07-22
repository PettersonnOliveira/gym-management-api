package com.portfolio.Gym.Management.API.Services;

import com.portfolio.Gym.Management.API.DTO.WorkoutRequestDTO;
import com.portfolio.Gym.Management.API.DTO.WorkoutResponseDTO;
import com.portfolio.Gym.Management.API.DTO.WorkoutUpdateDTO;
import com.portfolio.Gym.Management.API.Entities.Workout;
import com.portfolio.Gym.Management.API.Repositories.WorkoutRepository;
import org.springframework.stereotype.Service;
import com.portfolio.Gym.Management.API.Exceptions.ResourceNotFoundException;
import java.util.List;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

//    CREATE
//    Receber RequestDTO → Criar Entity → Salvar no banco → Receber Entity salva → Converter para
//    ResponseDTO → Retornar.
    public WorkoutResponseDTO createWorkout(WorkoutRequestDTO inputData){
        Workout workout = new Workout(inputData);
        Workout workoutSalva = workoutRepository.save(workout);
        return new WorkoutResponseDTO(workoutSalva);
    }


//            LIST ALL
//    Buscar todos os registros → Receber List<Entity> → Converter cada Entity para ResponseDTO →
//    Retornar lista.
    public List<WorkoutResponseDTO> listAll(){
        List<Workout> workouts = workoutRepository.findAll();
        return workouts.stream()
                .map(WorkoutResponseDTO::new)
                .toList();
    }


//    FIND BY ID
//    Receber ID → Buscar no banco → Se não existir, lançar exceção → Converter para ResponseDTO
//→ Retornar.
    public WorkoutResponseDTO findById(Long id){
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout não encontrado"));

       return new WorkoutResponseDTO(workout);
    }


//            UPDATE
//    Receber ID e UpdateDTO → Buscar entidade → Atualizar campos → Salvar novamente →
//    Converter para ResponseDTO → Retornar.

    public WorkoutResponseDTO update(Long id, WorkoutUpdateDTO updateData){
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout não encontrado"));
        workout.setName(updateData.name());
        workout.setDescription(updateData.description());
        workout.setDuration(updateData.duration());
        Workout workoutUpdate = workoutRepository.save(workout);
        return new WorkoutResponseDTO(workoutUpdate);

    }

//            DELETE
//    Receber ID → Buscar entidade → Excluir → Finalizar.

    public void delete(Long id){
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout não encontrado"));
        workoutRepository.delete(workout);
    }
}
