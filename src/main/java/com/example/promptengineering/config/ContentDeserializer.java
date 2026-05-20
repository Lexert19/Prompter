package com.example.promptengineering.config;

import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.model.ImageContent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.lang.reflect.Type;

public class ContentDeserializer extends JsonDeserializer<Content> {
    @Override
    public Content deserialize(JsonParser p, DeserializationContext ct) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode typeNode = node.get("type");

        if (typeNode == null || typeNode.isNull()) {
            throw new IOException("Lack of type in class Content");
        }

        String type = typeNode.asText();
        return switch (type) {
            case "text" -> p.getCodec().treeToValue(node, TextContent.class);
            case "image" -> p.getCodec().treeToValue(node, ImageContent.class);
            default -> throw new IOException("Undefined content type: " + type);
        };
    }
}
