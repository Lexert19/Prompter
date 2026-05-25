package com.example.promptengineering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hosted_nodes")
@Getter
@Setter
@NoArgsConstructor
public class HostedNode {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private User owner;

    private String nodeName;
    private String modelName;
    private String modelFamily;

    @Column(unique = true, nullable = false)
    private String authToken;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OFFLINE;

    private Instant lastHeartbeat;
    private String clientVersion;
    private String publicIp;

    private double pointsPer1kInput = 0.05;
    private double pointsPer1kOutput = 0.15;
    private long totalRequests = 0;
    private long totalTokens = 0;
    private boolean allowPublicUse = true;

    public enum Status {
        OFFLINE, ONLINE, BUSY, ERROR
    }
}
