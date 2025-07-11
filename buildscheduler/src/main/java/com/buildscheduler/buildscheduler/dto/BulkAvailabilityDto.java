package com.buildscheduler.buildscheduler.dto;

import lombok.Getter; import lombok.Setter;
import java.util.List;

@Getter @Setter
public class BulkAvailabilityDto {
    private List<AvailabilitySlotDto> slots;
    private List<Long> slotIdsToDelete;
}
