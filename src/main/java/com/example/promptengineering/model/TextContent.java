package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class TextContent extends Content {
    private String text;

    @Override
    public Map<String, Object> toMap(ProviderStrategy strategy, boolean cached) {
        return strategy.formatTextContent(this, cached);
    }

    @Override
    public int estimateTokens() {
        return (text != null) ? text.length() / 4 : 0;
    }
}
