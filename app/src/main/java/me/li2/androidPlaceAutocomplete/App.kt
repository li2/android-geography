package me.li2.androidPlaceAutocomplete

import androidx.multidex.MultiDexApplication
import me.li2.androidPlaceAutocomplete.MainComponent.appModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.conf.ConfigurableKodein

class App : MultiDexApplication(), KodeinAware {
    override val kodein = ConfigurableKodein(mutable = true)
    private var overrideModule: Kodein.Module? = null

    override fun onCreate() {
        super.onCreate()
        context = this
        setupKodein()
    }

    private fun setupKodein() {
        kodein.apply {
            clear()
            addImport(androidXModule(this@App))
            addImport(appModule, allowOverride = true)
            overrideModule?.let {
                addImport(it, true)
            }
        }
    }

    companion object {
        lateinit var context: App
    }
}
