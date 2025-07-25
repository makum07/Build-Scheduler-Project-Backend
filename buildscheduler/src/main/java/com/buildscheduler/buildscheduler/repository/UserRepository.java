package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    List<User> findByRoles_Name(String roleName);

    /**
     * Fetches a User entity along with all associated collections
     * required for the FullUserProfileDto, using JOIN FETCH to prevent N+1 issues
     * and ensure collections are initialized within the transaction.
     * Use DISTINCT to avoid duplicate User entities from Cartesian products.
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH u.skills s " +
            "LEFT JOIN FETCH u.certifications c " +
            "LEFT JOIN FETCH u.workerAvailabilitySlots was " +
            "LEFT JOIN FETCH u.workerAssignments wa " +
            "LEFT JOIN FETCH wa.assignedBy ab " + // Fetch the user who assigned the task
            "LEFT JOIN FETCH wa.subtask sub " + // Fetch the subtask for the assignment
            "LEFT JOIN FETCH sub.mainTask mt " + // Fetch the main task for the subtask
            "LEFT JOIN FETCH mt.project mp " + // Fetch the project for the main task
            "LEFT JOIN FETCH mt.equipmentManager " + // Fetch equipment manager for main task
            "LEFT JOIN FETCH mt.siteSupervisor " + // Fetch site supervisor for main task
            "LEFT JOIN FETCH sub.project " + // Fetch project for subtask
            "LEFT JOIN FETCH u.siteSupervisor ss " + // Direct ManyToOne relationships (who *this* user reports to)
            "LEFT JOIN FETCH u.projectManager pm " + // Direct ManyToOne relationships (who *this* user reports to)
            "LEFT JOIN FETCH u.managedTeam mdt " + // OneToMany relationships (ProjectManager's managed team)
            "LEFT JOIN FETCH u.managedProjects mpr " + // ProjectManager's managed projects
            "LEFT JOIN FETCH mpr.projectManager " + // Manager of the managed project (needed for ManagedProjects list)
            "LEFT JOIN FETCH mpr.siteSupervisor " + // SiteSupervisor of the managed project (needed for ManagedProjects list)
            "LEFT JOIN FETCH mpr.equipmentManager " + // EquipmentManager of the managed project (needed for ManagedProjects list)
            "LEFT JOIN FETCH mpr.workers " + // Workers assigned to the project itself (if applicable)
            "LEFT JOIN FETCH mpr.mainTasks mpr_mt " + // Main tasks of the managed project
            "LEFT JOIN FETCH mpr_mt.subtasks mpr_sub " + // Subtasks of main tasks of managed project
            "LEFT JOIN FETCH mpr_sub.workerAssignments mpr_wa " + // Worker assignments of subtasks of managed project
            "LEFT JOIN FETCH u.supervisedWorkers sw " + // SiteSupervisor's supervised workers
            "LEFT JOIN FETCH u.supervisedTasks st " + // SiteSupervisor's supervised tasks
            "LEFT JOIN FETCH st.subtasks st_sub " + // Subtasks of supervised tasks
            "LEFT JOIN FETCH st_sub.workerAssignments st_wa " + // Worker assignments of subtasks of supervised tasks
            "LEFT JOIN FETCH u.managedEquipment me " + // EquipmentManager's managed equipment
            "WHERE u.id = :userId")
    Optional<User> findFullProfileById(@Param("userId") Long userId);

    /**
     * Fetches a User entity by username along with all associated collections
     * required for the FullUserProfileDto.
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH u.skills s " +
            "LEFT JOIN FETCH u.certifications c " +
            "LEFT JOIN FETCH u.workerAvailabilitySlots was " +
            "LEFT JOIN FETCH u.workerAssignments wa " +
            "LEFT JOIN FETCH wa.assignedBy ab " +
            "LEFT JOIN FETCH wa.subtask sub " +
            "LEFT JOIN FETCH sub.mainTask mt " +
            "LEFT JOIN FETCH mt.project mp " +
            "LEFT JOIN FETCH mt.equipmentManager " +
            "LEFT JOIN FETCH mt.siteSupervisor " +
            "LEFT JOIN FETCH sub.project " +
            "LEFT JOIN FETCH u.siteSupervisor ss " +
            "LEFT JOIN FETCH u.projectManager pm " +
            "LEFT JOIN FETCH u.managedTeam mdt " +
            "LEFT JOIN FETCH u.managedProjects mpr " +
            "LEFT JOIN FETCH mpr.projectManager " +
            "LEFT JOIN FETCH mpr.siteSupervisor " +
            "LEFT JOIN FETCH mpr.equipmentManager " +
            "LEFT JOIN FETCH mpr.workers " +
            "LEFT JOIN FETCH mpr.mainTasks mpr_mt " +
            "LEFT JOIN FETCH mpr_mt.subtasks mpr_sub " +
            "LEFT JOIN FETCH mpr_sub.workerAssignments mpr_wa " +
            "LEFT JOIN FETCH u.supervisedWorkers sw " +
            "LEFT JOIN FETCH u.supervisedTasks st " +
            "LEFT JOIN FETCH st.subtasks st_sub " +
            "LEFT JOIN FETCH st_sub.workerAssignments st_wa " +
            "LEFT JOIN FETCH u.managedEquipment me " +
            "WHERE u.username = :username")
    Optional<User> findFullProfileByUsername(@Param("username") String username);

    /**
     * Custom query to find the Project Managers and Site Supervisors for a given worker.
     * This helps populate the 'worksUnder' field for a worker.
     * It looks for projects associated with the subtasks a worker is assigned to.
     */
    @Query("SELECT DISTINCT p.projectManager, p.siteSupervisor " +
            "FROM WorkerAssignment wa " +
            "JOIN wa.subtask s " +
            "JOIN s.mainTask mt " +
            "JOIN mt.project p " +
            "WHERE wa.worker.id = :workerId")
    List<Object[]> findProjectManagersAndSiteSupervisorsForWorker(@Param("workerId") Long workerId);
}