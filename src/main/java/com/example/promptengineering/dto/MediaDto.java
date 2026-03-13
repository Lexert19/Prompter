package com.example.promptengineering.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MediaDto {
    private Long id;
    private String fileName;
    private String url;
    private String contentType;
    private long size;
    private Instant uploadedAt;
}