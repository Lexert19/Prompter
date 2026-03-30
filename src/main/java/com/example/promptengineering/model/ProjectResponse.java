package com.example.promptengineering.model;

import com.example.promptengineering.dto.FileElementDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ProjectResponse {

    private Long id;
    private String name;
    private List<FileElementDTO> files;

    public ProjectResponse() {
        this.files = new ArrayList<>();
    }

    public ProjectResponse(Long id, String name, List<FileElementDTO> files) {
        this.id = id;
        this.name = name;
        this.files = (files != null) ? files : new ArrayList<>();
    }

}
