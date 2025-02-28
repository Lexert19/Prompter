package com.example.promptengineering.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.promptengineering.converter.HashMapToJsonConverter;

import jakarta.persistence.Convert;
import lombok.Data;

@Table("user")
@Data
public class User implements OAuth2User {
    @Id
    @PrimaryKey
    private String id;
    private String email;
    private String password;
    private String claudeKey;
    private String chatgptKey;
    private String nvidiaKey;
    private String geminiKey;

    @Column("keys_map")
    private HashMap<String, String> keys;

    public User(String id, String email, String password) {
        this.id = id;
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
