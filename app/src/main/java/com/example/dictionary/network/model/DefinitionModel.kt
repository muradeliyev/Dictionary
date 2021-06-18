package com.example.dictionary.network.model

data class DefinitionModel(
    val definition: String,
    val synonyms: List<String>?,
    val example: String?
)
