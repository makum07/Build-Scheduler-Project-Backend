package com.buildscheduler.buildscheduler.dto.project_manager;

import lombok.Data;
import java.util.List;

@Data
public class ProjectStructureResponse {
    private ProjectResponseDto project;
    private List<MainTaskResponseDto> mainTasks;
}