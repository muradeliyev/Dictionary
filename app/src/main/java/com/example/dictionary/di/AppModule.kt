package com.example.dictionary.di

import com.example.dictionary.network.api.IDictionaryApi
import com.example.dictionary.repository.DefinitionRepository
import com.example.dictionary.repository.DefinitionRepositoryImpl
import com.example.dictionary.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDictionaryApi(): IDictionaryApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IDictionaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDefinitionRepository(api: IDictionaryApi): DefinitionRepository {
        return DefinitionRepositoryImpl(api)
    }
}