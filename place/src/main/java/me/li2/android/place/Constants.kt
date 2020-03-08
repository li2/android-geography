/*
 * Created by Weiyi Li on 8/03/20.
 * https://github.com/li2
 */
package me.li2.android.place

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.Place

val RECTANGULAR_BOUNDS_AU =
        LatLngBounds(LatLng(-46.606400, 105.843059), LatLng(-11.086947, 158.124751))

internal val PLACE_DEFAULT_FIELDS = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS_COMPONENTS
)
