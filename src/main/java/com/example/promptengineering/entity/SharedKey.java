package com.example.promptengineering.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SharedKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;
    private String keyValue;
    private boolean working;
    private int usageCount;

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
}