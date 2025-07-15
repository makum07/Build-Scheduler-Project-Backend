package com.buildscheduler.buildscheduler.dto.worker;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class AvailabilitySlotDto {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
