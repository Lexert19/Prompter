package com.example.promptengineering.dto;

public record RegisterNodeRequest(String nodeName, String modelName, String modelFamily,
        Boolean allowPublicUse) {

}
