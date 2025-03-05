package com.example.promptengineering.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "user")
@Data
public class User implements OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    @Column(name = "keys", columnDefinition = "jsonb")
    private HashMap<String, String> keys;

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

}
