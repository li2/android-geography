package me.li2.android.geographysample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import me.li2.android.geographysample.databinding.GeographySampleFragmentBinding
import me.li2.android.location.LastKnownLocationUtils.requestLastKnownLocation
import me.li2.android.maps.MapType
import me.li2.android.maps.MapsStaticUtil.generateMapStaticImageUrl
import me.li2.android.maps.MarkerInfo
import me.li2.android.place.GeocoderUtils
import me.li2.android.place.PlaceAutoComplete
import me.li2.android.place.toAddressComponents

class GeographySampleFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: GeographySampleFragmentBinding
    private lateinit var placeAutoComplete: PlaceAutoComplete

    private val apiKey: String
        get() = context?.getString(R.string.google_api_key).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.geography_sample_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        placeAutoComplete = PlaceAutoComplete(view.context, apiKey)

        compositeDisposable += binding.btnGetLastKnownLocation.clicks().throttleFirstShort()
            .subscribe {
                doWithLocationPermission {
                    requestLastLocation()
                }
            }

        compositeDisposable += binding.btnLaunchAutocompleteActivity.clicks().subscribe {
            placeAutoComplete.launchPlaceAutocompleteActivity(requireActivity())
                .subscribeBy(onSuccess = { place ->
                    val result = place.toAddressComponents()
                    binding.autocompleteActivityResult =
                        "${result.suburbAndPostcode}\n${result.fullAddress}"
                }, onError = {
                    toast(it.message.toString())
                })
        }

        compositeDisposable += binding.etAutocompleteQuery
            .queryTextChanges()
            .switchMap { query -> placeAutoComplete.getPlacePredictions(query) }
            .forUi()
            .subscribeBy(onNext = { predictions ->
                val allPredictionsText = predictions.joinToString("\n") { it.getFullText(null) }
                binding.autocompletePredictionsResult = allPredictionsText
            }, onError = {
                toast(it.message.toString())
            })

        val marker1 = MarkerInfo("blue", listOf("62.107733,-145.5419"), 'S')
        val marker2 = MarkerInfo(
            "yellow",
            listOf("Tok, AK"),
            'C',
            icon = "https://raw.githubusercontent.com/li2/android-geography/master/panda.png"
        )
        val marker3 =
            MarkerInfo("green", listOf("Delta Junction, AK"), size = MarkerInfo.MarkerSize.TINY)
        binding.mapStaticUrl = generateMapStaticImageUrl(
            apiKey = apiKey,
            central = "63.259591,-144.667969",
            mapType = MapType.SATELLITE,
            markers = listOf(marker1, marker2, marker3),
            size = "400x400",
            zoomLevel = 6
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    private fun requestLastLocation() {
        compositeDisposable += requestLastKnownLocation(requireContext())
            .doOnSubscribe { binding.isLastKnownLocationLoading = true }
            .doOnTerminate { binding.isLastKnownLocationLoading = false }
            .subscribeBy(
                onNext = { location ->
                    binding.latLng = LatLng(location.latitude, location.longitude)
                    val addressComponents = GeocoderUtils.getAddressComponents(
                        requireContext(),
                        location.latitude,
                        location.longitude
                    )
                    binding.suburbAndPostCode = addressComponents?.suburbAndPostcode
                    binding.address = addressComponents?.fullAddress
                },
                onError = { exception ->
                    toast(exception.message.toString())
                })
    }
}
