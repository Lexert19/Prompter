package com.example.promptengineering.model;

import java.util.Map;

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
