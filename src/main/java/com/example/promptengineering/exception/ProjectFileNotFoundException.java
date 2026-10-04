package com.example.promptengineering.exception;

public class ProjectFileNotFoundException extends RuntimeException {
    private final Long fileId;

    public ProjectFileNotFoundException(Long fileId) {
        super("File not found: " + fileId);
        this.fileId = fileId;
    }

    public Long getFileId() {
        return fileId;
    }
}
