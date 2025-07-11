package com.buildscheduler.buildscheduler.service.custom;

import com.buildscheduler.buildscheduler.dto.AssignmentDto;
import com.buildscheduler.buildscheduler.model.Assignment;
import java.util.List;

public interface AssignmentService {
    Assignment assignWorker(AssignmentDto dto);
    List<Assignment> getWorkerAssignments(Long workerId);
    void removeAssignment(Long assignmentId);
}