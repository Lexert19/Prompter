package com.example.promptengineering.entity;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.internal.Json;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "users")
public class User implements OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    private Object keys;
    
    @Transient
    private HashMap<String, String> keysMap = new HashMap<>();
    
    
    public Object getKeys() {
        if (keysMap != null && !keysMap.isEmpty()) {
            try {
                return new ObjectMapper().writeValueAsString(keysMap);
            } catch (Exception e) {
                return "{}";
            }
        }
        return keys;
    }
    
    public void setKeys(Object keys) {
        this.keys = keys;
        try {
            if (keys != null) {
                String jsonString = keys instanceof String ? 
                    (String) keys : keys.toString();
                this.keysMap = new ObjectMapper().readValue(jsonString,
                        new TypeReference<HashMap<String, String>>() {});
            }
        } catch (Exception e) {
            this.keysMap = new HashMap<>();
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

    public Long getId() {
        return id;
    }

    public HashMap<String, String> getKeysMap() {
        return keysMap;
    }

    public void setKeysMap(HashMap<String, String> keysMap) {
        this.keysMap = keysMap;
    }

   


}
