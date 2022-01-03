package com.example.dictionary.repository

import com.example.dictionary.network.model.DictionarySingleResponseModel

interface DefinitionRepository {
    suspend fun getDefinitions(
        languageCode: String,
        word: String
    ): List<DictionarySingleResponseModel>
}