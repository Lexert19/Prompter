package com.example.promptengineering.model;

import com.example.promptengineering.dto.UserFileDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ProjectResponse {

    private Long id;
    private String name;
    private List<UserFileDTO> files;

    public ProjectResponse() {
        this.files = new ArrayList<>();
    }

    public ProjectResponse(Long id, String name, List<UserFileDTO> files) {
        this.id = id;
        this.name = name;
        this.files = (files != null) ? files : new ArrayList<>();
    }

}
