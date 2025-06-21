package com.ontrek.wear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val _token = MutableLiveData<String>("")
    val tokenState : LiveData<String> = _token

    fun saveToken(token: String) {
        _token.value = token
    }
}