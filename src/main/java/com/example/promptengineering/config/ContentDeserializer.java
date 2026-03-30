package com.example.promptengineering.config;

import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.model.ImageContent;
import com.google.gson.*;
import java.lang.reflect.Type;

public class ContentDeserializer implements JsonDeserializer<Content> {
    @Override
    public Content deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement typeElement = jsonObject.get("type");

        if (typeElement == null || typeElement.isJsonNull()) {
            throw new JsonParseException("Lack of type in class Content");
        }

        String type = typeElement.getAsString();

        switch (type) {
            case "text" :
                return context.deserialize(jsonObject, TextContent.class);
            case "image" :
                return context.deserialize(jsonObject, ImageContent.class);
            default :
                throw new JsonParseException("Undefined content type: " + type);
        }
    }
}
