package com.example.promptengineering.dto;

import lombok.Data;

@Data
public class SharedKeyInfoDto {
    private Long id;
    private String provider;
    private boolean working;
    private int usageCount;

    public SharedKeyInfoDto() {}

    public SharedKeyInfoDto(Long id, String provider, boolean working, int usageCount) {
        this.id = id;
        this.provider = provider;
        this.working = working;
        this.usageCount = usageCount;
    }
}
