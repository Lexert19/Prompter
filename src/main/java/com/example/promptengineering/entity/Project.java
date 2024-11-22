package com.example.promptengineering.entity;

import java.util.List;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.example.promptengineering.model.Embedding;
import com.example.promptengineering.model.FileElement;

@Table("project")
public class Project {
    @PrimaryKey
    private String id;
    private String name;
    private String userId;
    @CassandraType(type = CassandraType.Name.TEXT)
    private List<FileElement> files;
    @CassandraType(type = CassandraType.Name.TEXT)
    private List<Embedding> embeddings;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<Embedding> getEmbeddings() {
        return embeddings;
    }
    public void setEmbeddings(List<Embedding> embeddings) {
        this.embeddings = embeddings;
    }
    public List<FileElement> getFiles() {
        return files;
    }
    public void setFiles(List<FileElement> files) {
        this.files = files;
    }


    
}
