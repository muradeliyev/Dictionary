package com.example.dictionary.network.api

import com.example.dictionary.network.model.DictionarySingleResponseModel
import retrofit2.http.GET
import retrofit2.http.Path

interface IDictionaryApi {
    @GET("{language_code}/{word}")
    suspend fun getDefinitions(
        @Path("language_code") languageCode: String,
        @Path("word") word: String
    ): List<DictionarySingleResponseModel>
}