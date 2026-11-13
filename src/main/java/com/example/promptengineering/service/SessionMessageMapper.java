package com.example.promptengineering.service;

import com.example.promptengineering.model.ImageContent;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.model.ToolCall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SessionMessageMapper {

    public Message textMessage(String role, String text) {
        return new Message(role, List.of(new TextContent(text)));
    }

    public Message assistantMessage(String content, List<ToolCall> toolCalls) {
        Message message = new Message();
        message.setRole("assistant");
        message.setContent(content == null || content.isEmpty()
                ? List.of()
                : List.of(new TextContent(content)));
        if (toolCalls != null && !toolCalls.isEmpty()) {
            message.setToolCalls(toolCalls);
        }
        return message;
    }

    public Message toolMessage(String toolCallId, String name, String content) {
        Message message = new Message();
        message.setRole("tool");
        message.setToolCallId(toolCallId);
        message.setToolName(name);
        message.setStringContent(content);
        message.setContent(List.of());
        return message;
    }

    public Message toProviderMessage(SessionMessage sessionMessage) {
        Message original = sessionMessage.getProviderMessage();

        boolean hasImages = sessionMessage.getImages() != null
                && !sessionMessage.getImages().isEmpty();
        boolean hasDocuments = sessionMessage.getDocuments() != null
                && !sessionMessage.getDocuments().isEmpty();

        if (!hasImages && !hasDocuments) {
            return original;
        }

        Message clone = copyForEnrichment(original);
        prependDocumentMarkers(clone, sessionMessage.getDocuments());
        if (hasImages) {
            appendImages(clone, sessionMessage.getImages());
        }
        return clone;
    }

    private Message copyForEnrichment(Message original) {
        Message clone = new Message();
        clone.setRole(original.getRole());
        clone.setContent(new ArrayList<>(original.getContent()));
        clone.setToolCalls(original.getToolCalls());
        clone.setToolCallId(original.getToolCallId());
        clone.setToolName(original.getToolName());
        clone.setStringContent(original.getStringContent());
        clone.setCached(original.isCached());
        return clone;
    }

    private void prependDocumentMarkers(Message message, List<String> documents) {
        if (documents == null || documents.isEmpty())
            return;
        StringBuilder sb = new StringBuilder();
        for (String doc : documents) {
            sb.append("<files>").append(doc).append("</files>");
        }
        TextContent docContent = new TextContent();
        docContent.setType("text");
        docContent.setText(sb.toString());
        message.getContent().addFirst(docContent);
    }

    private void appendImages(Message message, List<String> imageIds) {
        for (String imgId : imageIds) {
            try {
                ImageContent ic = new ImageContent();
                ic.setType("image");
                ic.setFileId(Long.parseLong(imgId));
                message.getContent().add(ic);
            } catch (NumberFormatException e) {
                log.warn("Invalid image file ID in session: {}", imgId);
            }
        }
    }
}
