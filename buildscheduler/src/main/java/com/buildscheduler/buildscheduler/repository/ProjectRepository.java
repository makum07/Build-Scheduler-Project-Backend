package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.MainTask;
import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.Subtask;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findBySiteSupervisor(User siteSupervisor);
    @Query("SELECT mt FROM MainTask mt " +
            "LEFT JOIN FETCH mt.siteSupervisor " +
            "LEFT JOIN FETCH mt.equipmentManager " + // ✅ added
            "WHERE mt.project.id = :projectId " +
            "ORDER BY mt.id")
    List<MainTask> findMainTasksByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT st FROM Subtask st " +
            "LEFT JOIN FETCH st.mainTask " +
            "WHERE st.mainTask.id IN :mainTaskIds " +
            "ORDER BY st.mainTask.id, st.id")
    List<Subtask> findSubtasksByMainTaskIds(@Param("mainTaskIds") List<Long> mainTaskIds);

    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.projectManager " +
            "LEFT JOIN FETCH p.siteSupervisor " +
            "LEFT JOIN FETCH p.equipmentManager " +
            "WHERE p.id = :id")
    Optional<Project> findProjectWithManagers(@Param("id") Long id);

    @Query("SELECT p.id, " +
            "COUNT(st.id) AS totalSubtasks, " +
            "SUM(CASE WHEN st.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedSubtasks " +
            "FROM Project p " +
            "LEFT JOIN p.mainTasks mt " +
            "LEFT JOIN mt.subtasks st " +
            "WHERE p.id IN :projectIds " +
            "GROUP BY p.id")
    List<Object[]> getProjectCompletionStats(@Param("projectIds") List<Long> projectIds);

    Page<Project> findByProjectManager(User manager, Pageable pageable);


    // ========== ALTERNATIVE APPROACHES ==========

    /**
     * Single query with JOIN FETCH - can cause cartesian product for large datasets
     * Use this approach only for small datasets or when you absolutely need one query
     */
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.mainTasks mt " +
            "LEFT JOIN FETCH mt.subtasks st " +
            "LEFT JOIN FETCH mt.siteSupervisor mtss " +
            "LEFT JOIN FETCH p.projectManager pm " +
            "LEFT JOIN FETCH p.siteSupervisor pss " +
            "LEFT JOIN FETCH p.equipmentManager em " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithTasksAndSubtasksOptimized(@Param("id") Long id);

    /**
     * Entity Graph approach - good balance between performance and simplicity
     * Requires @NamedEntityGraph annotation on the Project entity
     */
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    @org.springframework.data.jpa.repository.EntityGraph(
            attributePaths = {
                    "mainTasks",
                    "mainTasks.subtasks",
                    "mainTasks.siteSupervisor",
                    "projectManager",
                    "siteSupervisor",
                    "equipmentManager"
            }
    )
    Optional<Project> findByIdWithEntityGraph(@Param("id") Long id);

    // ========== UTILITY QUERIES ==========

    /**
     * Get project with basic task structure (no subtasks)
     * Useful for lightweight operations
     */
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.mainTasks mt " +
            "LEFT JOIN FETCH p.projectManager pm " +
            "LEFT JOIN FETCH p.siteSupervisor ss " +
            "LEFT JOIN FETCH p.equipmentManager em " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithMainTasksOnly(@Param("id") Long id);

    /**
     * Get project with managers only (no tasks)
     * Useful for project summary views
     */
    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.projectManager " +
            "LEFT JOIN FETCH p.siteSupervisor " +
            "LEFT JOIN FETCH p.equipmentManager " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithUsers(@Param("id") Long id);

    /**
     * Count main tasks for a project
     */
    @Query("SELECT COUNT(mt) FROM MainTask mt WHERE mt.project.id = :projectId")
    long countMainTasksByProjectId(@Param("projectId") Long projectId);

    /**
     * Count subtasks for main tasks
     */
    @Query("SELECT COUNT(st) FROM Subtask st WHERE st.mainTask.id IN :mainTaskIds")
    long countSubtasksByMainTaskIds(@Param("mainTaskIds") List<Long> mainTaskIds);

    // ========== PERFORMANCE MONITORING QUERIES ==========

    /**
     * Get project completion statistics without loading full entities
     */
    @Query("SELECT " +
            "p.id, " +
            "p.title, " +
            "COUNT(DISTINCT mt.id) as mainTaskCount, " +
            "COUNT(DISTINCT st.id) as subtaskCount, " +
            "COUNT(DISTINCT CASE WHEN st.status = 'COMPLETED' THEN st.id END) as completedSubtaskCount " +
            "FROM Project p " +
            "LEFT JOIN p.mainTasks mt " +
            "LEFT JOIN mt.subtasks st " +
            "WHERE p.id = :id " +
            "GROUP BY p.id, p.title")
    Object[] getProjectCompletionStats(@Param("id") Long id);

    /**
     * Get projects with their completion percentages
     * Useful for dashboard views
     */
    @Query("SELECT " +
            "p.id, " +
            "p.title, " +
            "p.status, " +
            "p.endDate, " +
            "COUNT(DISTINCT st.id) as totalSubtasks, " +
            "COUNT(DISTINCT CASE WHEN st.status = 'COMPLETED' THEN st.id END) as completedSubtasks " +
            "FROM Project p " +
            "LEFT JOIN p.mainTasks mt " +
            "LEFT JOIN mt.subtasks st " +
            "WHERE p.projectManager = :manager " +
            "GROUP BY p.id, p.title, p.status, p.endDate")
    List<Object[]> getProjectsWithCompletionStats(@Param("manager") User manager);

    // ========== DEPRECATED QUERIES (for reference) ==========

    /**
     * @deprecated Use findByIdWithTasksAndSubtasksOptimized instead
     * This version can cause performance issues with large datasets
     */
    @Deprecated
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.mainTasks mt " +
            "LEFT JOIN FETCH mt.subtasks st " +
            "LEFT JOIN FETCH p.projectManager pm " +
            "LEFT JOIN FETCH p.siteSupervisor ss " +
            "LEFT JOIN FETCH p.equipmentManager em " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithTasksAndSubtasks(@Param("id") Long id);

    /**
     * @deprecated Use the multiple queries approach instead
     * This native query is harder to maintain and less portable
     */
    @Deprecated
    @Query(value = "SELECT DISTINCT p.*, " +
            "mt.id as mt_id, mt.title as mt_title, mt.description as mt_description, " +
            "mt.planned_start_date as mt_planned_start_date, mt.planned_end_date as mt_planned_end_date, " +
            "mt.actual_start_date as mt_actual_start_date, mt.actual_end_date as mt_actual_end_date, " +
            "mt.status as mt_status, mt.priority as mt_priority, " +
            "mt.estimated_hours as mt_estimated_hours, mt.actual_hours as mt_actual_hours, " +
            "st.id as st_id, st.title as st_title, st.status as st_status, " +
            "pm.id as pm_id, pm.username as pm_username, pm.email as pm_email, " +
            "ss.id as ss_id, ss.username as ss_username, ss.email as ss_email, " +
            "em.id as em_id, em.username as em_username, em.email as em_email, " +
            "mtss.id as mtss_id, mtss.username as mtss_username, mtss.email as mtss_email " +
            "FROM projects p " +
            "LEFT JOIN main_tasks mt ON p.id = mt.project_id " +
            "LEFT JOIN subtasks st ON mt.id = st.main_task_id " +
            "LEFT JOIN users pm ON p.manager_id = pm.id " +
            "LEFT JOIN users ss ON p.site_supervisor_id = ss.id " +
            "LEFT JOIN users em ON p.equipment_manager_id = em.id " +
            "LEFT JOIN users mtss ON mt.supervisor_id = mtss.id " +
            "WHERE p.id = :id", nativeQuery = true)
    List<Object[]> findProjectStructureNative(@Param("id") Long id);
}