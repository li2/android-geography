package me.li2.androidPlaceAutocomplete

data class PlaceAddressComponents(
        val name: String?,
        val fullAddress: String?,
        val streetNumber: String?,
        val route: String?,
        val city: String?,
        val state: String?,
        val postcode: String?,
        val country: String?) {
    fun getSuburbAndPostcode() = "$city, $postcode"
}
