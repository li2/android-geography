package me.li2.androidPlaceAutocomplete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_place_autocomplete.*
import me.li2.android.place.AddressComponents
import me.li2.android.place.PlaceAutoComplete
import me.li2.androidPlaceAutocomplete.databinding.FragmentPlaceAutocompleteBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class PlaceAutocompleteFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein(App.context)

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: FragmentPlaceAutocompleteBinding
    private val autoCompleteUtil by instance<PlaceAutoComplete>()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_place_autocomplete, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        compositeDisposable += btn_launch_autocomplete.clicks().subscribe {
            autoCompleteUtil.launchPlaceAutocompleteActivity(requireActivity())
                    .subscribeBy(onSuccess = { place ->
                        val result = AddressComponents.fromPlace(place)
                        binding.autocompleteActivityResult = "${result.suburbAndPostcode}\n${result.fullAddress}"
                    }, onError = {
                        toast(it.message.toString())
                    })
        }

        compositeDisposable += et_autocomplete_query
                .queryTextChanges()
                .switchMap { query -> autoCompleteUtil.getPlacePredictions(query) }
                .forUi()
                .subscribeBy(onNext = { predictions ->
                    val allPredictionsText = predictions.joinToString("\n") { it.getFullText(null) }
                    binding.autocompletePredictionsResult = allPredictionsText
                }, onError = {
                    toast(it.message.toString())
                })
    }
}
