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
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Search places with Google Places SDK in the following aspects:
 *
 * (1) two options to search place;
 * (2) use Rx to break with the OnActivityResult, callback implementation;
 *
 * @param context
 * @param apiKey google api key.
 *
 * @see <a href="https://developers.google.com/places/web-service/get-api-key">Get an API Key</a>
 * @see <a href="https://medium.com/@li2/android-practice-google-place-autocomplete-search-in-rx-79686271d840">Place Autocomplete in Rx</a>
 * @see <a href="https://developers.google.com/places/android-sdk/autocomplete">Place Autocomplete: official doc</a>
 */
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

    /**
     * Launch built-in autocomplete activity
     *
     * @param activity
     * @param initialQuery the initial query in the search input.
     * @param countryCode the country to restrict results to. This must be a list of ISO 3166-1 Alpha-2 country
     *                      codes (case insensitive). If no countries are set, no country filtering will take place.
     * @param typeFilter Filters the autocomplete results to the given place type.
     * @param fields the fields of the place to be requested.
     * @return A Place encapsulates information about a physical location, including its name, full address,
     *         latLng, address components like street number, route, suburb/locality, country, postal code if available.
     */
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
                .`as`(RxJavaBridge.toV3Single())
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

    /**
     * Get a list of place predictions.
     *
     * @param query
     * @param countryCode the country to restrict results to. This must be a list of ISO 3166-1 Alpha-2 country
     *                      codes (case insensitive). If no countries are set, no country filtering will take place.
     * @param typeFilter Filters the autocomplete results to the given place type.
     * @param locationBounds Sets the location where autocomplete predictions are to be biased towards.
     * @return A list of autocomplete suggestion of places, which includes the description of the suggested place
     *          as well as basic details including place ID and types.
     */
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

    /**
     * Get the details of a place.
     *
     * @param placeId the unique ID of the place to be requested. see [getId()](https://developers.google.com/places/android-sdk/reference/com/google/android/libraries/places/api/model/Place#getId())
     *                  for more details.
     * @return A place.
     */
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
