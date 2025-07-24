package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto; // Ensure this DTO exists
import com.buildscheduler.buildscheduler.model.MainTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// You might need to inject SubtaskMapper if MainTaskResponseDto includes subtasks
// @RequiredArgsConstructor // Uncomment if you inject other mappers
@Component
public class MainTaskMapper {

    // You might need a SubtaskMapper if MainTaskResponseDto needs to include subtask details
    // private final SubtaskMapper subtaskMapper;

    public MainTaskResponseDto toResponseDto(MainTask mainTask) {
        if (mainTask == null) {
            return null;
        }
        MainTaskResponseDto dto = new MainTaskResponseDto();
        dto.setId(mainTask.getId());
        dto.setTitle(mainTask.getTitle());
        dto.setDescription(mainTask.getDescription());

        dto.setStatus(mainTask.getStatus());
        dto.setEstimatedHours(mainTask.getEstimatedHours());


        // If MainTaskResponseDto contains subtasks, map them here:
        // if (mainTask.getSubtasks() != null) {
        //    dto.setSubtasks(mainTask.getSubtasks().stream()
        //        .map(subtaskMapper::toResponseDto) // Assuming subtaskMapper exists and has toResponseDto
        //        .collect(Collectors.toList()));
        // }

        return dto;
    }

    // You might also need a List conversion method
    // public List<MainTaskResponseDto> toResponseDtoList(Collection<MainTask> mainTasks) { ... }
}