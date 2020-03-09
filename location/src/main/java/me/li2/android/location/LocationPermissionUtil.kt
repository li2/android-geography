package me.li2.android.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

internal object LocationPermissionUtil {

    fun isLocationPermissionEnabled(context: Context): Boolean =
            (ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED)

    fun requestLocationPermission(activity: FragmentActivity): Observable<Permission> =
            RxPermissions(activity).requestEach(*listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION).toTypedArray())
}
