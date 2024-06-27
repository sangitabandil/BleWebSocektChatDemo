package com.demo.bleandwebsockethandson.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _showToolbarBack = MutableLiveData(false)
    val showToolbarBack: LiveData<Boolean> = _showToolbarBack

    fun handleToolbarBack(isShow : Boolean) = _showToolbarBack.postValue(isShow)
}