package com.example.promptengineering.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class SharedKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;
    private String keyValue;
    private boolean working;
    private int usageCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    private LocalDateTime blockedUntil;

    public SharedKey(String provider, String keyValue) {
        this.provider = provider;
        this.keyValue = keyValue;
        this.working = true;
        this.usageCount = 0;
    }

    public SharedKey() {
    }

    public void setProvider(String provider) {
        this.provider = provider != null ? provider.toLowerCase() : null;
    }

    public String getProvider() {
        return provider != null ? provider.toLowerCase() : null;
    }

    public void block(int minutes) {
        this.blockedUntil = LocalDateTime.now().plusMinutes(minutes);
        this.working = false;
    }

    public boolean isBlocked() {
        if (blockedUntil == null)
            return false;
        return LocalDateTime.now().isBefore(blockedUntil);
    }

    public void markWorking() {
        this.working = true;
        this.blockedUntil = null;
    }
}
