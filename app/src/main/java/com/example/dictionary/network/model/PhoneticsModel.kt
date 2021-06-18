package com.example.dictionary.network.model

import com.google.gson.annotations.SerializedName

data class PhoneticsModel(
    val text: String?,

    @SerializedName("audio")
    val audioUrl: String?
)
