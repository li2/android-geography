package me.li2.androidPlaceAutocomplete

import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import java.util.concurrent.TimeUnit

fun Observable<CharSequence>.mapToString(): Observable<String> = this.map { it.toString() }

fun <T> Observable<T>.throttleFirstShort() = this.throttleFirst(500L, TimeUnit.MILLISECONDS)!!

fun <T> Observable<T>.forUi(): Observable<T> =
        this.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

fun Fragment.toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

fun List<String>.containsIgnoreCase(key: String) = this.firstOrNull { it.equals(key, true) } != null

object MainComponent {
    val appModule = Kodein.Module("app module") {
        bind<PlaceAutoCompleteUtil>() with provider { PlaceAutoCompleteUtil(instance(), App.context.getString(R.string.google_api_key)) }
    }
}

/**
 * Return query text changes observable.
 *
 * - filter: to filter undesired text like blank text to avoid unnecessary API call.
 * - debounce: to ignore the previous items in the given time and only emit the last one, to avoid too much API calls.
 * - distinctUntilChanged: to avoid the duplicate API call.
 */
fun EditText.queryTextChanges(): Observable<String> {
    return textChanges()
            .map { it.toString() }
            .filter { it.isNotBlank() }
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
}
