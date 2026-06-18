package com.example.promptengineering.dto;

import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.HostedNode.Status;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PublicHostedNodeDto {
    private UUID id;
    private String nodeName;
    private String modelName;
    private String modelFamily;
    private Status status;
    private double pointsPer1kInput;
    private double pointsPer1kOutput;

    public static PublicHostedNodeDto fromEntity(HostedNode node) {
        return PublicHostedNodeDto.builder().id(node.getId()).nodeName(node.getNodeName())
                .modelName(node.getModelName()).modelFamily(node.getModelFamily())
                .status(node.getStatus()).pointsPer1kInput(node.getPointsPer1kInput())
                .pointsPer1kOutput(node.getPointsPer1kOutput()).build();
    }
}
