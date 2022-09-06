package com.simple.hotspotcreator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class HomeViewModel @Inject constructor() : ViewModel() {
    val isLoadingLiveData = MutableLiveData<Boolean>()

}