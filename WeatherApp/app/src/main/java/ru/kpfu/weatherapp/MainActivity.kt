package ru.kpfu.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.kpfu.weatherapp.gps.GpsService
import ru.kpfu.weatherapp.model.WeatherModel
import ru.kpfu.weatherapp.network.WeatherClient
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.getOrSet

class MainActivity : AppCompatActivity() {

    private lateinit var startStopBtn: FloatingActionButton
    private lateinit var progress: ProgressBar
    private lateinit var cityTv: AppCompatTextView
    private lateinit var tempTv: AppCompatTextView
    private lateinit var minTempTv: AppCompatTextView
    private lateinit var maxTempTv: AppCompatTextView
    private lateinit var feelTempTv: AppCompatTextView
    private lateinit var weatherIv: AppCompatImageView
    private lateinit var weatherDscrTv: AppCompatTextView
    private lateinit var windTv: AppCompatTextView
    private lateinit var sunriseTv: AppCompatTextView
    private lateinit var sunsetTv: AppCompatTextView

    private val locationListener: (Location) -> Unit = ::locationChanged
    private val weatherListener: (WeatherModel) -> Unit = ::weatherLoaded

    private val simpleDateFormatTimeThreadLocal = ThreadLocal<SimpleDateFormat>()
    private val simpleDateFormatTime
        get() = simpleDateFormatTimeThreadLocal.getOrSet {
            SimpleDateFormat("HH:mm", Locale("ru"))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MainViewModel.weatherData.observe(this, weatherListener)
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) requestLocationPermission() else changeServiceState()

        initViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0
            && permissions.first() == Manifest.permission.ACCESS_FINE_LOCATION
            && grantResults.first() == PackageManager.PERMISSION_GRANTED
        ) {
            changeServiceState(true)
        }
    }

    override fun onStop() {
        super.onStop()
        MainViewModel.location.removeObserver(locationListener)
    }

    override fun onStart() {
        super.onStart()
        if (GpsService.running)
            MainViewModel.location.observe(this, locationListener)
    }

    private fun initViews() {
        startStopBtn = findViewById<FloatingActionButton>(R.id.service_fab).apply {
            setOnClickListener {
                changeServiceState()
            }
        }

        progress = findViewById(R.id.progress)

        cityTv = findViewById(R.id.city_tv)
        tempTv = findViewById(R.id.temp_tv)
        minTempTv = findViewById(R.id.min_temp_tv)
        maxTempTv = findViewById(R.id.max_temp_tv)
        feelTempTv = findViewById(R.id.temp_feel_tv)
        weatherIv = findViewById(R.id.weather_iv)
        weatherDscrTv = findViewById(R.id.weather_state_tv)
        windTv = findViewById(R.id.wind_speed_tv)
        sunriseTv = findViewById(R.id.sunrise_time_tv)
        sunsetTv = findViewById(R.id.sunset_time_tv)
    }

    private fun requestLocationPermission() {
        this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
    }

    private fun sendCommand(command: String) {
        val intent = Intent(this, GpsService::class.java).apply {
            this.action = command
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun locationChanged(l: Location) {
        WeatherClient.getWeatherData(l)
    }

    private fun weatherLoaded(weatherModel: WeatherModel) = with(weatherModel) {
        cityTv.text = name
        tempTv.text = getTempString(main.temp)
        minTempTv.text = getTempString(main.tempMin)
        maxTempTv.text = getTempString(main.tempMax)
        feelTempTv.text = getTempString(main.feelsLike)
        val iconId = when (weather[0].id) {
            in 200..299 -> R.drawable.ic_storm
            in 300..399 -> R.drawable.ic_rainy
            in 500..599 -> R.drawable.ic_heavy_rain
            in 600..699 -> R.drawable.ic_snow
            771 -> R.drawable.ic_umbrella
            781 -> R.drawable.ic_tornado
            800 -> R.drawable.ic_sunny
            in 801..804 -> R.drawable.ic_cloudy
            else -> R.drawable.ic_wind
        }
        weatherIv.setImageResource(iconId)
        weatherDscrTv.text = weather[0].description
        windTv.text = getString(R.string.wind_speed, wind.speed.toString())
        sunriseTv.text = sys.sunrise.formatTime()
        sunsetTv.text = sys.sunset.formatTime()
    }

    private fun getTempString(temp: Double): String {
        return temp.toInt().let {
            getString(R.string.temperature, if (it > 0) "+$it" else "$it")
        }
    }

    private fun Long.formatTime(): String {
        return simpleDateFormatTime.format(Date(this))
    }

    private fun changeServiceState(forceStart: Boolean = false) {
        if (!GpsService.running || forceStart) {
            sendCommand(Consts.START_LOCATION_SERVICE)
            MainViewModel.location.observe(this, locationListener)
            MainViewModel.weatherData.observe(this, weatherListener)
            MainViewModel.isError.observe(this) {
                if (it) {
                    Toast.makeText(this, "Произошла ошибка", Toast.LENGTH_LONG).show()
                }
            }
            MainViewModel.isProgress.observe(this) {
                progress.visibility = if (it) View.VISIBLE else View.GONE
            }
        } else {
            sendCommand(Consts.STOP_LOCATION_SERVICE)
            MainViewModel.location.removeObservers(this)
        }
    }
}