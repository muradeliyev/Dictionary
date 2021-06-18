package com.example.dictionary.network.model

data class DictionarySingleResponseModel(
    val word: String,
    val phonetics: List<PhoneticsModel>,
    val meanings: List<MeaningModel>
)
