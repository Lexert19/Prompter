package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class Content {
    private String type;
    private Long fileId;

    public abstract Map<String, Object> toMap(ProviderStrategy strategy, boolean cached);
    public abstract int estimateTokens();

}