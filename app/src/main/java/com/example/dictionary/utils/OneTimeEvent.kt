package com.example.dictionary.utils

sealed class OneTimeEvent {
    class OnGetDefinition : OneTimeEvent()
}