package com.example.dictionary.network.util

import com.example.dictionary.network.api.IDictionaryApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {
    val api: IDictionaryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/api/v2/entries/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IDictionaryApi::class.java)
    }
}