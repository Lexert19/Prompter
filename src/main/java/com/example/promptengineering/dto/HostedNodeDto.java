package com.example.promptengineering.dto;

import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.HostedNode.Status;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HostedNodeDto {
  private UUID id;
  private String nodeName;
  private String modelName;
  private String modelFamily;
  private String authToken;
  private Status status;
  private Instant lastHeartbeat;
  private String clientVersion;
  private String publicIp;
  private double pointsPer1kInput;
  private double pointsPer1kOutput;
  private long totalRequests;
  private long totalTokens;
  private boolean allowPublicUse;

  public static HostedNodeDto fromEntity(HostedNode node) {
    return HostedNodeDto.builder()
        .id(node.getId())
        .nodeName(node.getNodeName())
        .modelName(node.getModelName())
        .modelFamily(node.getModelFamily())
        .authToken(node.getAuthToken())
        .status(node.getStatus())
        .lastHeartbeat(node.getLastHeartbeat())
        .clientVersion(node.getClientVersion())
        .publicIp(node.getPublicIp())
        .pointsPer1kInput(node.getPointsPer1kInput())
        .pointsPer1kOutput(node.getPointsPer1kOutput())
        .totalRequests(node.getTotalRequests())
        .totalTokens(node.getTotalTokens())
        .allowPublicUse(node.isAllowPublicUse())
        .build();
  }
}