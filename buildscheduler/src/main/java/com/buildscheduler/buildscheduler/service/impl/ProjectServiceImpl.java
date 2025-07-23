package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.ProjectRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.MainTask;
import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.Subtask;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.MainTaskRepository;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.SubtaskRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.custom.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MainTaskRepository mainTaskRepository;
    private final SubtaskRepository subtaskRepository;

    // Manual constructor for dependency injection.
    // Lombok's @RequiredArgsConstructor can be used if all fields are 'final'.
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, UserMapper userMapper,
                              MainTaskRepository mainTaskRepository, SubtaskRepository subtaskRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mainTaskRepository = mainTaskRepository;
        this.subtaskRepository = subtaskRepository;
    }

    @Override
    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto dto, User manager) {
        Project project = new Project();
        mapDtoToEntity(dto, project);
        project.setProjectManager(manager);
        project.setStatus(Project.ProjectStatus.PLANNING);
        project = projectRepository.save(project);
        return mapEntityToDto(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        mapDtoToEntity(dto, project);
        project = projectRepository.save(project);
        return mapEntityToDto(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        ProjectResponseDto dto = mapEntityToDto(project);
        // Corrected: Use findByProjectIdIn and pass a list
        List<MainTask> mainTasks = mainTaskRepository.findByProjectIdIn(Collections.singletonList(project.getId()));

        List<Long> mainTaskIds = mainTasks.stream().map(MainTask::getId).collect(Collectors.toList());
        List<Subtask> subtasks = Collections.emptyList();
        if (!mainTaskIds.isEmpty()) {
            // Assuming SubtaskRepository has findByMainTaskIdIn(List<Long> mainTaskIds)
            subtasks = subtaskRepository.findByMainTaskIdIn(mainTaskIds);
        }

        Map<Long, List<Subtask>> subtasksByMainTask = subtasks.stream()
                .collect(Collectors.groupingBy(st -> st.getMainTask().getId()));

        double projectCompletion = calculateProjectCompletion(mainTasks, subtasksByMainTask);
        dto.setCompletionPercentage(roundToTwoDecimalPlaces(projectCompletion));
        dto.setOverdue(isProjectOverdue(project));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getProjectsByManager(User manager, Pageable pageable) {
        Page<Project> projectPage = projectRepository.findByProjectManager(manager, pageable);
        List<Project> projects = projectPage.getContent();

        if (projects.isEmpty()) {
            return projectPage.map(this::mapEntityToDto);
        }

        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        // Corrected: Use findByProjectIdIn and pass a list
        List<MainTask> allMainTasks = mainTaskRepository.findByProjectIdIn(projectIds);
        Map<Long, List<MainTask>> mainTasksByProjectId = allMainTasks.stream()
                .collect(Collectors.groupingBy(mt -> mt.getProject().getId()));

        List<Long> allMainTaskIds = allMainTasks.stream().map(MainTask::getId).toList();
        List<Subtask> allSubtasks = Collections.emptyList();
        if (!allMainTaskIds.isEmpty()) {
            // Assuming SubtaskRepository has findByMainTaskIdIn(List<Long> mainTaskIds)
            allSubtasks = subtaskRepository.findByMainTaskIdIn(allMainTaskIds);
        }
        Map<Long, List<Subtask>> subtasksByMainTaskId = allSubtasks.stream()
                .collect(Collectors.groupingBy(st -> st.getMainTask().getId()));


        return projectPage.map(project -> {
            ProjectResponseDto dto = mapEntityToDto(project);

            List<MainTask> currentProjectMainTasks = mainTasksByProjectId.getOrDefault(project.getId(), Collections.emptyList());

            double projectCompletion = calculateProjectCompletion(currentProjectMainTasks, subtasksByMainTaskId);
            dto.setCompletionPercentage(roundToTwoDecimalPlaces(projectCompletion));
            dto.setOverdue(isProjectOverdue(project));
            return dto;
        });
    }

    private boolean isProjectOverdue(Project project) {
        if (project.getStatus() == Project.ProjectStatus.COMPLETED) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return project.getEndDate() != null && project.getEndDate().isBefore(today);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    @Transactional
    public void assignSupervisor(Long projectId, Long supervisorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", supervisorId));
        project.setSiteSupervisor(supervisor);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void assignEquipmentManager(Long projectId, Long equipmentManagerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User equipmentManager = userRepository.findById(equipmentManagerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", equipmentManagerId));
        project.setEquipmentManager(equipmentManager);
        projectRepository.save(project);
    }

    // ─── DTO ↔ ENTITY Mapping ─────────────────────────────────────────────────

    private void mapDtoToEntity(ProjectRequestDto src, Project dest) {
        dest.setTitle(src.getTitle());
        dest.setDescription(src.getDescription());
        dest.setStartDate(src.getStartDate());
        dest.setEndDate(src.getEndDate());
        dest.setEstimatedBudget(src.getEstimatedBudget());
        dest.setLocation(src.getLocation());
        dest.setPriority(src.getPriority());
    }

    private ProjectResponseDto mapEntityToDto(Project entity) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setProjectManagerId(entity.getProjectManager().getId());
        dto.setProjectManagerName(entity.getProjectManager().getUsername());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        dto.setEstimatedBudget(entity.getEstimatedBudget());
        dto.setLocation(entity.getLocation());
        dto.setPriority(entity.getPriority());

        if (entity.getSiteSupervisor() != null) {
            dto.setSiteSupervisor(userMapper.toSimpleUserDto(entity.getSiteSupervisor()));
        }
        if (entity.getEquipmentManager() != null) {
            dto.setEquipmentManager(userMapper.toSimpleUserDto(entity.getEquipmentManager()));
        }
        return dto;
    }

    // --- Completion Calculation Methods (Moved from ProjectStructureService for project-level rollups) ---

    // This method provides a status-based completion percentage for a subtask.
    // If you need to factor in 'actualHoursSpent' here, you'd need to ensure 'workerAssignments'
    // are loaded for the Subtask object, possibly by fetching the subtasks with an EntityGraph
    // or through a transactional context that initializes the lazy collection.
    private double calculateSubtaskCompletion(Subtask subtask) {
        if (subtask == null || subtask.getStatus() == null) return 0.0;

        return switch (subtask.getStatus()) {
            case COMPLETED -> 100.0;
            case IN_PROGRESS -> 50.0;
            case ASSIGNED -> 25.0;
            case ON_HOLD -> 10.0;
            case DELAYED -> 5.0;
            default -> 0.0;
        };
    }

    private double calculateMainTaskCompletion(List<Subtask> subtasks, MainTask mainTask) {
        if (subtasks == null || subtasks.isEmpty()) {
            // Fallback to MainTask status if no subtasks
            return switch (mainTask.getStatus()) {
                case COMPLETED -> 100.0;
                case IN_PROGRESS -> 50.0;
                case ON_HOLD -> 10.0;
                case DELAYED -> 5.0;
                default -> 0.0;
            };
        }

        return subtasks.stream()
                .mapToDouble(this::calculateSubtaskCompletion)
                .average()
                .orElse(0.0);
    }

    private double calculateProjectCompletion(List<MainTask> mainTasks, Map<Long, List<Subtask>> subtasksByMainTask) {
        if (mainTasks == null || mainTasks.isEmpty()) {
            return 0.0;
        }

        return mainTasks.stream()
                .mapToDouble(mainTask -> calculateMainTaskCompletion(subtasksByMainTask.getOrDefault(mainTask.getId(), Collections.emptyList()), mainTask))
                .average()
                .orElse(0.0);
    }
}