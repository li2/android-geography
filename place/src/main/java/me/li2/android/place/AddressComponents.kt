/*
 * Created by Weiyi Li on 8/03/20.
 * https://github.com/li2
 */
package me.li2.android.place

import com.google.android.libraries.places.api.model.Place

data class AddressComponents(
        val name: String,
        val fullAddress: String,
        val streetNumber: String,
        val route: String,
        val city: String,
        val state: String,
        val postcode: String,
        val country: String) {

    val suburbAndPostcode
        get() = when {
            city.isNotEmpty() && postcode.isNotEmpty() -> "$city, $postcode"
            city.isEmpty() && postcode.isNotEmpty() -> postcode
            else -> city
        }

    companion object {
        fun fromPlace(place: Place) =
                AddressComponents(
                        name = place.name.orEmpty(),
                        fullAddress = place.address.orEmpty(),
                        streetNumber = place.getAddressComponentName("street_number").orEmpty(),
                        route = place.getAddressComponentName(Place.Type.ROUTE.toString()).orEmpty(),
                        city = place.getAddressComponentName(Place.Type.LOCALITY.toString()).orEmpty(),
                        state = place.getAddressComponentName(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.toString()).orEmpty(),
                        postcode = place.getAddressComponentName(Place.Type.POSTAL_CODE.toString()).orEmpty(),
                        country = place.getAddressComponentName(Place.Type.COUNTRY.toString()).orEmpty())

        private fun Place.getAddressComponentName(type: String): String? {
            return addressComponents?.asList()?.firstOrNull { addressComponent ->
                addressComponent.types.containsIgnoreCase(type)
            }?.name
        }

        private fun List<String>.containsIgnoreCase(key: String) =
                this.firstOrNull { it.equals(key, true) } != null
    }
}
