package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.AvailabilitySlot;
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
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    Optional<AvailabilitySlot> findByUserAndDate(User user, LocalDate date);

    @Query("SELECT a FROM AvailabilitySlot a WHERE a.user = :user AND a.date = :date " +
            "AND a.startTime <= :startTime AND a.endTime >= :endTime")
    Optional<AvailabilitySlot> findContainingSlot(
            @Param("user") User user,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT a FROM AvailabilitySlot a WHERE a.user = :user " +
            "AND a.date BETWEEN :start AND :end")
    List<AvailabilitySlot> findByUserInDateRange(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<AvailabilitySlot> findByUser(User user);
}