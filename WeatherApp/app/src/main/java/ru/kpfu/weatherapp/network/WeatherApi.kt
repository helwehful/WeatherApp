package ru.kpfu.weatherapp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.kpfu.weatherapp.model.WeatherModel

interface WeatherApi {

    companion object {
        private const val API_KEY = "c897b438605eff38e5df5f6928351367"
    }

    @GET("weather")
    fun getWeatherByCoords(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "ru"
    ): Call<WeatherModel>
}