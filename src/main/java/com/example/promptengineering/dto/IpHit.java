package com.example.promptengineering.dto;

import java.time.Instant;

public record IpHit(
        Instant time,
        String ip
) {}