package com.example.technanas.data.model

data class UsefulLink(
    val id: Long,
    val title: String,
    val description: String,
    val url: String,
    val category: LinkCategory
)
