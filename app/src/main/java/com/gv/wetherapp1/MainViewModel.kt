package com.gv.wetherapp1

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gv.wetherapp1.adapters.WeatherModel

class MainViewModel: ViewModel() {
val liveDataCurrent=MutableLiveData<WeatherModel>()
val liveDataList=MutableLiveData<List<WeatherModel>>()

}