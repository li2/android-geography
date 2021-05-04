package me.li2.android.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

/**
 * https://developer.android.com/training/location/retrieve-current
 */
internal class FusedLocationProvider(context: Context) : LifecycleObserver {

    private val client = LocationServices.getFusedLocationProviderClient(context)
    private var isRequesting = false
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    fun requestLastLocation(): Observable<Location> {
        return Observable.create { emitter ->
            client.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    emitter.onNext(requireNotNull(task.result))
                    emitter.onComplete()
                } else {
                    startLocationUpdates(emitter)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(emitter: ObservableEmitter<Location>) {
        if (isRequesting) return
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location = locationResult?.locations?.firstOrNull()
                stopLocationUpdates()
                if (location != null) {
                    emitter.onNext(location)
                    emitter.onComplete()
                } else {
                    emitter.onError(LastKnownLocationNotFoundException)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {}
        }

        // request location updates only once
        val locationRequest = LocationRequest().apply {
            numUpdates = 1
            priority = PRIORITY_HIGH_ACCURACY
        }
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        emitter.setCancellable { stopLocationUpdates() }
        isRequesting = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            client.removeLocationUpdates(locationCallback)
            isRequesting = false
        }
    }
}
