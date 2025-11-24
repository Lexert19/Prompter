package com.example.promptengineering.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "file_element")
public class FileElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "file_element_pages", joinColumns = @JoinColumn(name = "file_element_id"))
    @Column(name = "page_content")
    private List<String> pages;

    @Transient
    private List<List<Double>> vectors;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public List<String> getPages() {
        return pages;
    }
    public void setPages(List<String> pages) {
        this.pages = pages;
    }
    public List<List<Double>> getVectors() {
        return vectors;
    }
    public void setVectors(List<List<Double>> vectors) {
        this.vectors = vectors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
