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
import me.li2.android.location.LastKnownLocationUtils.requestLastKnownLocation
import me.li2.android.location.RequestLocationResult
import me.li2.android.location.ifLocationAllowed
import me.li2.android.location.openAppSettings
import me.li2.android.location.openSystemLocationSetting
import me.li2.android.place.GeocoderUtils
import me.li2.android.place.PlaceAutoComplete
import me.li2.android.place.toAddressComponents
import me.li2.android.placesample.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: FragmentMainBinding
    private lateinit var placeAutoComplete: PlaceAutoComplete

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        placeAutoComplete = PlaceAutoComplete(view.context, view.context.getString(R.string.google_api_key))

        compositeDisposable += btn_get_last_known_location.clicks().throttleFirstShort().subscribe {
            activity?.ifLocationAllowed(onError = {
                toast(it.message.toString())
            }, onResult = { result: RequestLocationResult ->
                when (result) {
                    RequestLocationResult.ALLOWED -> {
                        // location permission granted and service is on,
                        // it's good time to get last know location
                        requestLastLocation()
                    }
                    RequestLocationResult.PERMISSION_DENIED,
                    RequestLocationResult.PERMISSION_DENIED_NOT_ASK_AGAIN -> {
                        // location permission denied, go to App settings
                        activity?.openAppSettings(BuildConfig.APPLICATION_ID)
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
