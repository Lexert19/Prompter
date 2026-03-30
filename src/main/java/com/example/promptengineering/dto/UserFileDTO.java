package com.example.promptengineering.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserFileDTO {
    private Long id;
    private String fileName;
    private String contentType;
    private long size;
    private Long ownerId;

    public UserFileDTO() {
    }

    public UserFileDTO(Long id, String fileName, String contentType, long size, Long ownerId) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.ownerId = ownerId;
    }

}
