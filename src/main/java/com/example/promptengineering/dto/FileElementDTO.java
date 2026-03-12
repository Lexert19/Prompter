package com.example.promptengineering.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileElementDTO {
    private Long id;
    private String name;

    public FileElementDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
