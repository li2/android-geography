/*
 * Created by Weiyi Li on 8/03/20.
 * https://github.com/li2
 */
package me.li2.android.place

import android.content.Context
import android.location.Address
import android.location.Geocoder
import timber.log.Timber.e
import java.io.IOException
import java.util.*

/**
 * Geocoder Utils
 * https://developer.android.com/training/location/display-address#create-geocoder
 */
object GeocoderUtils {

    private fun Address.isValid(): Boolean =
            maxAddressLineIndex >= 0
                    && thoroughfare != null
                    && hasLatitude()
                    && hasLongitude()

    // only accept address which has thoroughfare
    private fun getAddressComponents(addresses: List<Address>): AddressComponents? {
        val address = addresses.firstOrNull { it.isValid() } ?: return null
        return AddressComponents(
                fullAddress = address.getAddressLine(0),
                streetNumber = address.subThoroughfare,
                street = address.thoroughfare,
                streetType = address.thoroughfare.substringAfterLast(" "),
                city = address.locality,
                state = address.adminArea,
                country = address.countryName,
                postcode = address.postalCode,
                latitude = address.latitude,
                longitude = address.longitude)
    }

    /**
     * Return [AddressComponents] from latitude and longitude.
     */
    fun getAddressComponents(context: Context, lat: Double, lng: Double): AddressComponents? {
        return try {
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 10)
            getAddressComponents(addresses)
        } catch (ioe: IOException) {
            e(ioe, "network or other I/O problems.")
            null
        } catch (iae: IllegalArgumentException) {
            e(iae, "invalid lat or lng values: $lat, $lng")
            null
        }
    }

    /**
     * Return [AddressComponents] from address full name.
     */
    fun getAddressComponents(context: Context, addressName: String): AddressComponents? {
        return try {
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocationName(addressName, 10)
            getAddressComponents(addresses)
        } catch (ioe: IOException) {
            e(ioe, "network or other I/O problems.")
            null
        }
    }
}
