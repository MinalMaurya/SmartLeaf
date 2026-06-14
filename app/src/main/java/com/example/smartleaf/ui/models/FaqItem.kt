package com.example.smartleaf.ui.models

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val tags: List<String> = emptyList()
)