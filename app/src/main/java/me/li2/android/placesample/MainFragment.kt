package me.li2.android.placesample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_main.*
import me.li2.android.common.framework.openAppSettings
import me.li2.android.location.LastKnownLocationUtils.requestLastKnownLocation
import me.li2.android.location.RequestLocationResult
import me.li2.android.location.ifLocationAllowed
import me.li2.android.location.openSystemLocationSetting
import me.li2.android.maps.MapType
import me.li2.android.maps.MapsStaticUtil.generateMapStaticImageUrl
import me.li2.android.maps.MarkerInfo
import me.li2.android.place.GeocoderUtils
import me.li2.android.place.PlaceAutoComplete
import me.li2.android.place.toAddressComponents
import me.li2.android.placesample.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: FragmentMainBinding
    private lateinit var placeAutoComplete: PlaceAutoComplete

    private val apiKey: String
        get() = context?.getString(R.string.google_api_key).orEmpty()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        placeAutoComplete = PlaceAutoComplete(view.context, apiKey)

        compositeDisposable += btn_get_last_known_location.clicks().throttleFirstShort().subscribe {
            activity?.ifLocationAllowed(locationPermissionPrompt(requireContext()), onError = {
                toast(it.message.toString())
            }, onResult = { result: RequestLocationResult ->
                when (result) {
                    RequestLocationResult.ALLOWED -> {
                        // location permission granted and service is on,
                        // it's good time to get last know location
                        requestLastLocation()
                    }
                    RequestLocationResult.PERMISSION_DENIED -> {
                        toast("permission denied ${System.currentTimeMillis()}")
                    }
                    RequestLocationResult.PERMISSION_DENIED_NOT_ASK_AGAIN -> {
                        // location permission denied, go to App settings
                        activity?.openAppSettings(view.context.packageName)
                    }
                    RequestLocationResult.SERVICE_OFF -> {
                        // location service is turned off, go to system settings
                        activity?.openSystemLocationSetting { isServiceOn -> }
                    }
                }
            })
        }

        compositeDisposable += btn_launch_autocomplete_activity.clicks().subscribe {
            placeAutoComplete.launchPlaceAutocompleteActivity(requireActivity())
                    .subscribeBy(onSuccess = { place ->
                        val result = place.toAddressComponents()
                        binding.autocompleteActivityResult = "${result.suburbAndPostcode}\n${result.fullAddress}"
                    }, onError = {
                        toast(it.message.toString())
                    })
        }

        compositeDisposable += et_autocomplete_query
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
        val marker2 = MarkerInfo("yellow", listOf("Tok, AK"), 'C', icon = "https://raw.githubusercontent.com/li2/android-geography/master/panda.png")
        val marker3 = MarkerInfo("green", listOf("Delta Junction, AK"), size = MarkerInfo.MarkerSize.TINY)
        binding.mapStaticUrl = generateMapStaticImageUrl(
            apiKey = apiKey,
            central = "63.259591,-144.667969",
            mapType = MapType.SATELLITE,
            markers = listOf(marker1, marker2, marker3),
            size = "400x400",
            zoomLevel = 6)
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
                            val addressComponents = GeocoderUtils.getAddressComponents(requireContext(), location.latitude, location.longitude)
                            binding.suburbAndPostCode = addressComponents?.suburbAndPostcode
                            binding.address = addressComponents?.fullAddress
                        },
                        onError = { exception ->
                            toast(exception.message.toString())
                        })
    }
}
