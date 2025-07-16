package com.buildscheduler.buildscheduler.service.custom;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MainTaskService {
    MainTaskResponseDto createMainTask(MainTaskRequestDto dto, Long projectId);
    MainTaskResponseDto updateMainTask(Long id, MainTaskRequestDto dto);
    void deleteMainTask(Long id);
    Page<MainTaskResponseDto> getMainTasksByProject(Long projectId, Pageable pageable);
}