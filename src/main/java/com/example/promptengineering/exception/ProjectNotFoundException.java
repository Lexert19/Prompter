package com.example.promptengineering.exception;

public class ProjectNotFoundException extends RuntimeException {
    private final Long projectId;

    public ProjectNotFoundException(Long projectId) {
        super("Project not found: " + projectId);
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }
}
