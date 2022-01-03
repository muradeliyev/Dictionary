package com.example.dictionary.utils

sealed class State {
    object Loading : State()
    class Success<T>(val data: T) : State()
    class Error(val message: String) : State()
    object NoInternet : State()
}