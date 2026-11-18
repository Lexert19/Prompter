package com.example.promptengineering.model;

public record UrlCitation(String url, String title, String content,
                          Integer startIndex, Integer endIndex) {
}