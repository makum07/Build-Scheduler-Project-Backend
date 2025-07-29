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
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.workerAvailabilitySlots WHERE u.id = :id")
    Optional<User> findByIdWithAvailabilitySlots(@Param("id") Long id);
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
            "LEFT JOIN FETCH mt.equipmentManager " +
            "LEFT JOIN FETCH mt.siteSupervisor " +
            "LEFT JOIN FETCH u.siteSupervisor ss " +
            "LEFT JOIN FETCH u.projectManager pm_user " + // This is the crucial line for direct PM relationship
            "LEFT JOIN FETCH u.managedTeam mdt " +
            "LEFT JOIN FETCH u.managedProjects mpr " +
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
            "LEFT JOIN FETCH mt.equipmentManager " +
            "LEFT JOIN FETCH mt.siteSupervisor " +
            "LEFT JOIN FETCH u.siteSupervisor ss " +
            "LEFT JOIN FETCH u.projectManager pm_user " + // This is the crucial line for direct PM relationship
            "LEFT JOIN FETCH u.managedTeam mdt " +
            "LEFT JOIN FETCH u.managedProjects mpr " +
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