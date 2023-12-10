package com.example.dplm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dplm.adapter.weatherModel

class MainViewModel:ViewModel() {
    val liveDataCurrent = MutableLiveData<weatherModel>()
    val liveDataList = MutableLiveData<List<weatherModel>>()

}