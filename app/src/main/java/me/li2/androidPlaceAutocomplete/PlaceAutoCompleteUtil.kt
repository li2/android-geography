package me.li2.androidPlaceAutocomplete

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import io.reactivex.Observable
import io.reactivex.Single

class PlaceAutoCompleteUtil(private val context: Context, apiKey: String) {

    private val defaultCountryCode
        get() = context.resources.configuration.locale.country

    private val placesClient: PlacesClient

    init {
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey)
        }
        placesClient = Places.createClient(context)
    }

    fun launchPlaceAutocompleteActivity(
            activity: FragmentActivity,
            initialQuery: String = "",
            countryCode: String = defaultCountryCode,
            typeFilter: TypeFilter = TypeFilter.REGIONS,
            fields: List<Place.Field> = PLACE_FIELDS
    ): Single<Place> {
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setInitialQuery(initialQuery)
                .setCountry(countryCode)
                .setTypeFilter(typeFilter)
                .build(activity)
        return RxActivityResult(activity)
                .start(intent)
                .map { result ->
                    when (result.resultCode) {
                        Activity.RESULT_OK -> Autocomplete.getPlaceFromIntent(result.data)
                        AutocompleteActivity.RESULT_ERROR -> {
                            val status = Autocomplete.getStatusFromIntent(result.data)
                            throw IllegalStateException("Place is not found caused by ${status.statusMessage}")
                        }
                        else -> throw IllegalStateException("The user canceled the operation")
                    }
                }
    }

    fun getPlacePredictions(
            query: String,
            countryCode: String = defaultCountryCode,
            typeFilter: TypeFilter = TypeFilter.ADDRESS,
            locationBounds: LatLngBounds = RECTANGULAR_BOUNDS_AU)
            : Observable<List<AutocompletePrediction>> {
        return Observable.create { emitter ->
            val request = FindAutocompletePredictionsRequest
                    .builder()
                    .setLocationBias(RectangularBounds.newInstance(locationBounds))
                    .setCountry(countryCode)
                    .setTypeFilter(typeFilter)
                    .setSessionToken(AutocompleteSessionToken.newInstance())
                    .setQuery(query)
                    .build()
            placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        emitter.onNext(response.autocompletePredictions)
                        emitter.onComplete()
                    }
                    .addOnFailureListener { exception ->
                        emitter.onError(exception)
                    }
        }
    }

    fun getPlaceById(placeId: String): Observable<Place> {
        return Observable.create { emitter ->
            val request = FetchPlaceRequest.builder(placeId, PLACE_FIELDS).build()
            placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        emitter.onNext(response.place)
                    }
                    .addOnFailureListener { exception ->
                        emitter.onError(exception)
                    }
        }
    }

    companion object {
        private val RECTANGULAR_BOUNDS_AU = LatLngBounds(LatLng(-46.606400, 105.843059), LatLng(-11.086947, 158.124751))
        private val PLACE_FIELDS = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS)

        fun parsePlace(place: Place) = PlaceAddressComponents(
                name = place.name,
                fullAddress = place.address,
                streetNumber = place.getAddressComponentName("street_number"),
                route = place.getAddressComponentName(Place.Type.ROUTE.toString()),
                city = place.getAddressComponentName(Place.Type.LOCALITY.toString()),
                state = place.getAddressComponentName(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.toString()),
                postcode = place.getAddressComponentName(Place.Type.POSTAL_CODE.toString()),
                country = place.getAddressComponentName(Place.Type.COUNTRY.toString())
        )

        private fun Place.getAddressComponentName(type: String): String? {
            return addressComponents?.asList()?.firstOrNull { addressComponent ->
                addressComponent.types.containsIgnoreCase(type)
            }?.name
        }
    }
}
