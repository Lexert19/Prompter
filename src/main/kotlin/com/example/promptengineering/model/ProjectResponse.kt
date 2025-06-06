package com.example.promptengineering.model

import com.example.promptengineering.dto.FileElementDTO
import com.example.promptengineering.entity.FileElement


data class ProjectResponse(
    val id: String,
    val name: String,
    val files: List<FileElementDTO> = emptyList()
)