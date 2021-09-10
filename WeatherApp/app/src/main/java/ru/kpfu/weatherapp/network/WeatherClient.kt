package ru.kpfu.weatherapp.network

import android.location.Location
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.kpfu.weatherapp.Consts
import ru.kpfu.weatherapp.MainViewModel
import ru.kpfu.weatherapp.model.WeatherModel
import java.util.concurrent.TimeUnit

object WeatherClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("TAG_NETWORK", message)
                }
            }).apply { level = HttpLoggingInterceptor.Level.BODY }
        ).build()

    private val api: WeatherApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(Consts.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    fun getWeatherData(l: Location) {
        MainViewModel.isProgress.value = true
        api.getWeatherByCoords(l.latitude, l.longitude)
            .enqueue(object : Callback<WeatherModel> {
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    MainViewModel.isProgress.value = false
                    MainViewModel.isError.value = !response.isSuccessful
                    if (response.isSuccessful) {
                        MainViewModel.weatherData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                    MainViewModel.isProgress.value = false
                    MainViewModel.isError.value = true
                    t.printStackTrace()
                }
            })
    }
}