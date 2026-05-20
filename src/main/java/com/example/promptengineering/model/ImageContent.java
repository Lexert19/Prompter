package com.example.promptengineering.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageContent extends Content {
    private String mediaType;
    private String data;

    @Override
    public Map<String, Object> toMap(ProviderStrategy strategy, boolean cached) {
        return strategy.formatImageContent(this, cached);
    }

    @Override
    public int estimateTokens() {
        return 300;
    }
}
