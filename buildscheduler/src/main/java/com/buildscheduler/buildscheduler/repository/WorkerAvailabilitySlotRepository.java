package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.WorkerAvailabilitySlot;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerAvailabilitySlotRepository extends JpaRepository<WorkerAvailabilitySlot, Long> {

    Optional<WorkerAvailabilitySlot> findByUserAndDate(User user, LocalDate date);

    @Query("""
      SELECT s 
        FROM WorkerAvailabilitySlot s 
       WHERE s.user      = :user 
         AND s.date      = :date
         AND s.startTime <= :startTime 
         AND s.endTime   >= :endTime
    """)
    Optional<WorkerAvailabilitySlot> findContainingSlot(
            @Param("user")      User user,
            @Param("date")      LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime")   LocalTime endTime
    );

    @Query("""
      SELECT s 
        FROM WorkerAvailabilitySlot s 
       WHERE s.user = :user 
         AND s.date BETWEEN :start AND :end
    """)
    List<WorkerAvailabilitySlot> findByUserInDateRange(
            @Param("user")  User user,
            @Param("start") LocalDate start,
            @Param("end")   LocalDate end
    );

    List<WorkerAvailabilitySlot> findByUser(User user);

}
