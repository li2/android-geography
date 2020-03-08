/*
 * Created by Weiyi Li on 8/03/20.
 * https://github.com/li2
 */
package me.li2.android.place

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
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

class PlaceAutoComplete(private val context: Context, apiKey: String) {

    // related to language setting
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
            fields: List<Place.Field> = PLACE_DEFAULT_FIELDS
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
            locationBounds: LatLngBounds? = null): Observable<List<AutocompletePrediction>> {
        return Observable.create { emitter ->
            val request = FindAutocompletePredictionsRequest
                    .builder()
                    .setCountry(countryCode)
                    .setTypeFilter(typeFilter)
                    .setSessionToken(AutocompleteSessionToken.newInstance())
                    .setQuery(query)
                    .apply {
                        locationBounds?.let {
                            setLocationBias(RectangularBounds.newInstance(it))
                        }
                    }
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
            val request = FetchPlaceRequest.builder(placeId, PLACE_DEFAULT_FIELDS).build()
            placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        emitter.onNext(response.place)
                    }
                    .addOnFailureListener { exception ->
                        emitter.onError(exception)
                    }
        }
    }
}
