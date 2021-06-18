package com.example.dictionary.network.model

data class MeaningModel(
    val partOfSpeech: String,
    val definitions: List<DefinitionModel>
)
