package com.buildscheduler.buildscheduler.dto.site_supervisor;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SimpleEquipmentDto {
    private Long id;
    private String name;
    private String model;
    private String serialNumber;
    private String type;
    private String status;
}