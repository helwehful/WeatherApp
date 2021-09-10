package ru.kpfu.weatherapp.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

class GpsListener private constructor() : LocationListener {

    private var locationManager: LocationManager? = null
    private var locationCallback: ((Location) -> Unit)? = null

    companion object {
        private var instance: GpsListener? = null
        fun getInstance() = instance ?: GpsListener().also { instance = it }
    }

    fun startLocationListening(context: Context, locationCallback: (Location) -> Unit) {
        locationManager = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
            .also { locationManager ->
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                this.locationCallback = locationCallback
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000L,
                    30F,
                    this
                )
            }
    }

    fun stopLocating() {
        locationManager?.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        locationCallback?.invoke(location)
    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onProviderEnabled(provider: String) {

    }
}
