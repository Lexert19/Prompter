package com.example.promptengineering.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "model")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    //@Column(columnDefinition = "TEXT")
    private String text;

    private String provider;

    private String url;

    private boolean global;

    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;



    public Model() {}
    public Model(String name, String text, String provider, String url, String type, User user) {
        this.name = name;
        this.text = text;
        this.provider = provider;
        this.url = url;
        this.type = type;
        this.user = user;
    }

}