package me.li2.android.location

import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy


internal object LocationServiceUtil {

    fun isLocationServiceEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestLocationService(activity: FragmentActivity): Observable<Boolean> {
        return Observable.create { emitter ->
            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationRequest.create().apply { priority = LocationRequest.PRIORITY_HIGH_ACCURACY })
                    .addLocationRequest(LocationRequest.create().apply { priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY })
                    .setAlwaysShow(true)
            val result = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
            result.addOnSuccessListener {
                // All location settings are satisfied. The client can initialize location requests here.
                emitter.onNext(true)
                emitter.onComplete()
            }
            result.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        // Show the dialog by calling startResolutionForResult() in Rx
                        RxActivityResult(activity).start(exception.resolution)
                            .`as`(RxJavaBridge.toV3Single())
                            .subscribeBy(onSuccess = {
                                // resultCode is RESULT_CANCELED when user click Ok, this issue happens on Pixel2 Android 10, it works on Nexus5 Android 6
                                // https://issuetracker.google.com/issues/118347902
                                emitter.onNext(isLocationServiceEnabled(activity))
                                emitter.onComplete()
                        }, onError = {
                            emitter.onError(it)
                        })
                    } catch (exception: IntentSender.SendIntentException) {
                        emitter.onError(exception)
                    }
                } else {
                    emitter.onNext(false)
                    emitter.onComplete()
                }
            }
        }
    }
}
