package com.example.dictionary.viewmodels

import androidx.lifecycle.*
import com.example.dictionary.repository.DefinitionRepository
import com.example.dictionary.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repo: DefinitionRepository
) : ViewModel() {

    private val _definition = MutableLiveData<State>()
    val definition: LiveData<State>
        get() = _definition

    fun getDefinition(word: String) {
        _definition.value = State.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val definitions = repo.getDefinitions("en_US", word)
                _definition.postValue(State.Success(definitions[0]))
            } catch (e: IOException) {
                _definition.postValue(State.NoInternet)
            } catch (e: HttpException) {
                _definition.postValue(State.Error(word))
            }
        }
    }

}