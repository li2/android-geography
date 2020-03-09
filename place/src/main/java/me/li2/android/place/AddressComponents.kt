/*
 * Created by Weiyi Li on 8/03/20.
 * https://github.com/li2
 */
package me.li2.android.place

data class AddressComponents(
        val placeName: String = "",
        val fullAddress: String = "",
        val streetNumber: String = "",
        val street: String = "",
        val streetType: String = "",
        val city: String = "",
        val state: String = "",
        val postcode: String = "",
        val country: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null) {

    val suburbAndPostcode
        get() = when {
            city.isNotEmpty() && postcode.isNotEmpty() -> "$city, $postcode"
            city.isEmpty() && postcode.isNotEmpty() -> postcode
            else -> city
        }
}
