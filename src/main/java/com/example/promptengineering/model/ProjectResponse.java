package com.example.promptengineering.model;

import com.example.promptengineering.dto.FileElementDTO;
import java.util.ArrayList;
import java.util.List;

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FileElementDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FileElementDTO> files) {
        this.files = files;
    }
}