package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class Content {
    private String type;
    private String mediaType;
    private String data;
    private Long fileId;

    public abstract Map<String, Object> toMap(ProviderStrategy strategy, boolean cached);
    public abstract int estimateTokens();
//    public Map<String, Object> toMap(ProviderStrategy  provider, boolean cached) {
//        switch(type) {
//            case "text" -> {
//                return handleText(provider, cached);
//            }
//            case "image" -> {
//                return handleImage(provider, cached);
//            }
//            default -> throw new IllegalStateException("Unknown content type");
//        }
//    }
//
//    private Map<String, Object> handleText(ProviderStrategy  provider, boolean cache) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("type", "text");
//        map.put("text", text);
//
//        if (cache && provider.equals("ANTHROPIC")) {
//            map.put("cache_control", new CacheControl().toMap());
//        }
//        return map;
//    }
//
//    private Map<String, Object> handleImage(ProviderStrategy  provider, boolean cache) {
//        if (provider.equals("ANTHROPIC")) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("type", "image");
//            map.put("source", Map.of(
//                "type", "base64",
//                "media_type", mediaType,
//                "data", data
//            ));
//
//            if (cache) {
//                map.put("cache_control", new CacheControl().toMap());
//            }
//            return map;
//        } else{
//            return Map.of(
//                "type", "image_url",
//                "image_url", Map.of(
//                    "url", "data:" + mediaType + ";base64," + data
//                )
//            );
//        }
//
//
//        //throw new UnsupportedOperationException("Base64 images not supported for " + provider);
//    }


}