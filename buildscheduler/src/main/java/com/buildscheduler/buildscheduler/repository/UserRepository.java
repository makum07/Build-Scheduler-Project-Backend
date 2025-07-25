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

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH u.skills s " +
            "LEFT JOIN FETCH u.certifications c " +
            "LEFT JOIN FETCH u.workerAvailabilitySlots was " +
            "LEFT JOIN FETCH u.workerAssignments wa " +
            "LEFT JOIN FETCH wa.assignedBy ab " +
            "LEFT JOIN FETCH wa.subtask sub " +
            "LEFT JOIN FETCH sub.mainTask mt " +
            "LEFT JOIN FETCH mt.project mp_sub " + // Alias for subtask's project
            "LEFT JOIN FETCH mt.equipmentManager " +
            "LEFT JOIN FETCH mt.siteSupervisor " +
            "LEFT JOIN FETCH sub.project sub_p " + // Alias for subtask's direct project
            "LEFT JOIN FETCH u.siteSupervisor ss " +
            "LEFT JOIN FETCH u.projectManager pm_user " + // Alias for user's direct project manager
            "LEFT JOIN FETCH u.managedTeam mdt " + // Project Manager's directly managed team (from users.project_manager_id)
            "LEFT JOIN FETCH u.managedProjects mpr " + // Project Manager's managed projects
            "LEFT JOIN FETCH mpr.siteSupervisor mpr_ss " + // Site Supervisors of managed projects
            "LEFT JOIN FETCH mpr.equipmentManager mpr_em " + // Equipment Managers of managed projects
            "LEFT JOIN FETCH mpr.workers mpr_wrk " + // Workers directly assigned to managed projects
            "LEFT JOIN FETCH mpr.mainTasks mpr_mt " + // Main tasks of managed projects
            "LEFT JOIN FETCH mpr_mt.subtasks mpr_sub " + // Subtasks of main tasks of managed projects
            "LEFT JOIN FETCH mpr_sub.workerAssignments mpr_wa " + // Worker assignments for subtasks in managed projects
            "LEFT JOIN FETCH mpr_wa.worker mpr_wa_wrk " + // Workers in assignments of managed project subtasks
            "LEFT JOIN FETCH u.supervisedWorkers sw " +
            "LEFT JOIN FETCH u.supervisedTasks st " +
            "LEFT JOIN FETCH st.subtasks st_sub " +
            "LEFT JOIN FETCH st_sub.workerAssignments st_wa " +
            "LEFT JOIN FETCH u.managedEquipment me " +
            "WHERE u.id = :userId")
    Optional<User> findFullProfileById(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH u.skills s " +
            "LEFT JOIN FETCH u.certifications c " +
            "LEFT JOIN FETCH u.workerAvailabilitySlots was " +
            "LEFT JOIN FETCH u.workerAssignments wa " +
            "LEFT JOIN FETCH wa.assignedBy ab " +
            "LEFT JOIN FETCH wa.subtask sub " +
            "LEFT JOIN FETCH sub.mainTask mt " +
            "LEFT JOIN FETCH mt.project mp_sub " +
            "LEFT JOIN FETCH mt.equipmentManager " +
            "LEFT JOIN FETCH mt.siteSupervisor " +
            "LEFT JOIN FETCH sub.project sub_p " +
            "LEFT JOIN FETCH u.siteSupervisor ss " +
            "LEFT JOIN FETCH u.projectManager pm_user " +
            "LEFT JOIN FETCH u.managedTeam mdt " +
            "LEFT JOIN FETCH u.managedProjects mpr " +
            "LEFT JOIN FETCH mpr.siteSupervisor mpr_ss " +
            "LEFT JOIN FETCH mpr.equipmentManager mpr_em " +
            "LEFT JOIN FETCH mpr.workers mpr_wrk " +
            "LEFT JOIN FETCH mpr.mainTasks mpr_mt " +
            "LEFT JOIN FETCH mpr_mt.subtasks mpr_sub " +
            "LEFT JOIN FETCH mpr_sub.workerAssignments mpr_wa " +
            "LEFT JOIN FETCH mpr_wa.worker mpr_wa_wrk " +
            "LEFT JOIN FETCH u.supervisedWorkers sw " +
            "LEFT JOIN FETCH u.supervisedTasks st " +
            "LEFT JOIN FETCH st.subtasks st_sub " +
            "LEFT JOIN FETCH st_sub.workerAssignments st_wa " +
            "LEFT JOIN FETCH u.managedEquipment me " +
            "WHERE u.username = :username")
    Optional<User> findFullProfileByUsername(@Param("username") String username);

    @Query("SELECT DISTINCT p.projectManager, p.siteSupervisor " +
            "FROM WorkerAssignment wa " +
            "JOIN wa.subtask s " +
            "JOIN s.mainTask mt " +
            "JOIN mt.project p " +
            "WHERE wa.worker.id = :workerId")
    List<Object[]> findProjectManagersAndSiteSupervisorsForWorker(@Param("workerId") Long workerId);
}