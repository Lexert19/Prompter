package com.example.promptengineering.entity;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.nimbusds.oauth2.sdk.Role;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User implements OAuth2User {
    @Id
    private String id;
    private String email;
    private String password;
    private HashMap<String, String> keys = new HashMap<>();
    private List<Role> roles = new ArrayList<>();

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    

    public User() {
    }



    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "id", id,
                "email", email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return this.email;
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

    public HashMap<String, String> getKeys() {
        return keys;
    }

    public void setKeys(HashMap<String, String> keysMap) {
        this.keys = keysMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public List<Role> getRoles() {
        return roles;
    }



    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

}
