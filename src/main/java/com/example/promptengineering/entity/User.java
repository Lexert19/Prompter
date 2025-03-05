package com.example.promptengineering.entity;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "users")
public class User implements OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    @Column(name = "keys", columnDefinition = "jsonb")
    private String keys;

    @Transient
    private HashMap<String, String> keysMap = new HashMap<>();

    @PrePersist
    @PreUpdate
    public void beforeSave() throws JsonProcessingException {
        if (keysMap != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            this.keys = objectMapper.writeValueAsString(keysMap);
        }
    }

    @PostLoad
    public void afterLoad() throws IOException {
        if (keys != null && !keys.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            this.keysMap = objectMapper.readValue(keys,
                    new TypeReference<HashMap<String, String>>() {
                    });
        }
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "id", id,
                "email", email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return this.email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String, String> getKeysMap() {
        return keysMap;
    }

    public void setKeysMap(HashMap<String, String> keys) {
        this.keysMap = keys;
    }

}
