package me.li2.android.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes.RESOLUTION_REQUIRED
import com.google.android.gms.location.LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE
import me.li2.android.location.LocationServiceUtil.isLocationServiceEnabled
import timber.log.Timber.i
import timber.log.Timber.w

/**
 NOT NEEDED ANYMORE, because we can use RxActivityResult to send pendingIntent instead of creating this transparent theme activity.
 see [LocationServiceUtil.requestLocationService]
 */
private class RequestLocationSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationSetting()
    }

    private fun requestLocationSetting() {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().apply { priority = LocationRequest.PRIORITY_HIGH_ACCURACY })
                .addLocationRequest(LocationRequest.create().apply { priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY })
                .setAlwaysShow(true)
        val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
                i("All location settings are satisfied. The client can initialize location requests here.")
                setRequestCheckResult(true)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    RESOLUTION_REQUIRED -> {
                        try {
                            val resolvable = exception as? ResolvableApiException
                            resolvable?.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                            i("Show the dialog by calling startResolutionForResult()")
                        } catch (exception: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (exception: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    }
                    SETTINGS_CHANGE_UNAVAILABLE -> {
                        setRequestCheckResult(false)
                        w("Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            i("Check the resolution result: $resultCode")
            // resultCode is 0 when click OK the first time. unreliable.
            setRequestCheckResult(isLocationServiceEnabled(this))
        }
    }

    private fun setRequestCheckResult(isTurnedOn: Boolean) {
        setResult(if (isTurnedOn) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        finish()
        i("setRequestCheckResult: $isTurnedOn")
    }

    companion object {
        private const val KEY_RESULT_IS_TURNED_ON = "key_is_turned_on"
        private const val REQUEST_CHECK_SETTINGS = 111

        fun requestLocationSettingIntent(context: Context): Intent =
                Intent(context, RequestLocationSettingActivity::class.java)

        fun isLocationSettingTurnedOn(resultIntent: Intent): Boolean =
                resultIntent.getBooleanExtra(KEY_RESULT_IS_TURNED_ON, false)
    }
}