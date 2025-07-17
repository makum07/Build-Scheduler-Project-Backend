package com.buildscheduler.buildscheduler.dto.project_manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor  // Add this annotation
public class ProjectStructureResponse {
    private FullProjectResponseDto project;
    private List<MainTaskResponseDto> mainTasks;
}