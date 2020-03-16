/*
 * Created by Weiyi Li on 12/03/20.
 * https://github.com/li2
 */
package me.li2.android.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import me.li2.android.common.framework.isLocationPermissionGranted
import me.li2.android.location.LocationServiceUtil.isLocationServiceEnabled

object LastKnownLocationUtils {
    /**
     * Return true if Google Play Service is installed on device.
     * To call API need to setup google play service: https://developers.google.com/android/guides/setup
     */
    private fun isGooglePlayServiceAvailable(context: Context) =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS


    /**
     * Last known location changes observable.
     * Location can be null in the following situations:
     * (1) location service is turned off;
     * (2) no location permission;
     * (3) the device never recorded its location;
     * (4) Google Play services on the device has restarted.
     * so it's necessary to wrapper nullable Location with Resource.
     */
    @SuppressLint("MissingPermission")
    fun requestLastKnownLocation(context: Context): Observable<Location> {
        return Observable.create { emitter ->
            if (!isLocationServiceEnabled(context)) {
                emitter.onError(LocationServiceTurnedOffException)
            }
            if (!context.isLocationPermissionGranted()) {
                emitter.onError(LocationPermissionDeniedException)
            }

            // approach 1: use LocationManager if play service is not available on this device
            val location =
                    if (!isGooglePlayServiceAvailable(context)) {
                        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    } else {
                        null
                    }

            // approach 2: use the fused location provider to retrieve the device's last known location.
            var fusedLocationDisposable: Disposable? = null
            if (location != null) {
                emitter.onNext(location)
                emitter.onComplete()
            } else {
                fusedLocationDisposable = FusedLocationProvider(context).requestLastLocation().subscribeBy(onNext = { fusedLocation ->
                    emitter.onNext(fusedLocation)
                    emitter.onComplete()
                }, onError = {
                    emitter.onError(it)
                })
            }

            emitter.setCancellable {
                fusedLocationDisposable?.dispose()
            }
        }
    }
}
