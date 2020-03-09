/*
 * Created by Weiyi Li on 12/03/20.
 * https://github.com/li2
 */
package me.li2.android.place

import com.google.android.libraries.places.api.model.Place

fun Place.toAddressComponents() =
        AddressComponents(
                placeName = this.name.orEmpty(),
                fullAddress = this.address.orEmpty(),
                streetNumber = this.getAddressComponentName("street_number").orEmpty(),
                street = this.getAddressComponentName(Place.Type.ROUTE.toString()).orEmpty(),
                city = this.getAddressComponentName(Place.Type.LOCALITY.toString()).orEmpty(),
                state = this.getAddressComponentName(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.toString()).orEmpty(),
                postcode = this.getAddressComponentName(Place.Type.POSTAL_CODE.toString()).orEmpty(),
                country = this.getAddressComponentName(Place.Type.COUNTRY.toString()).orEmpty(),
                latitude = this.latLng?.latitude,
                longitude = this.latLng?.longitude)

private fun Place.getAddressComponentName(type: String): String? {
    return addressComponents?.asList()?.firstOrNull { addressComponent ->
        addressComponent.types.containsIgnoreCase(type)
    }?.name
}

private fun List<String>.containsIgnoreCase(key: String) =
        this.firstOrNull { it.equals(key, true) } != null

