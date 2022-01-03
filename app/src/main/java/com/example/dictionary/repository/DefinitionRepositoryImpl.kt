package com.example.dictionary.repository

import com.example.dictionary.network.api.IDictionaryApi
import com.example.dictionary.network.model.DictionarySingleResponseModel
import javax.inject.Inject


class DefinitionRepositoryImpl @Inject constructor(
    private val api: IDictionaryApi
) : DefinitionRepository {

    override suspend fun getDefinitions(
        languageCode: String,
        word: String
    ): List<DictionarySingleResponseModel> {
        return api.getDefinitions(languageCode, word)
    }

}
