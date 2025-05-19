package com.example.todoapp.models

data class Tarea(
    val id: String = "",
    val title: String,
    val description: String,
    val createdBy: String
)
