package ru.kpfu.weatherapp

import android.location.Location
import androidx.lifecycle.MutableLiveData
import ru.kpfu.weatherapp.model.WeatherModel

object MainViewModel {
    val location: MutableLiveData<Location> = MutableLiveData()
    val weatherData: MutableLiveData<WeatherModel> = MutableLiveData()
    val isError: MutableLiveData<Boolean> = MutableLiveData(false)
    val isProgress: MutableLiveData<Boolean> = MutableLiveData(true)
}