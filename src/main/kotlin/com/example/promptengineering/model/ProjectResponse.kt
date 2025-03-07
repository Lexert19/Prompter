package com.example.promptengineering.model

data class ProjectResponse(
    val id: String,
    val name: String,
    val files: List<FileElement> 
)