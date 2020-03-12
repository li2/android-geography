[![](https://jitpack.io/v/li2/android-place.svg)](https://jitpack.io/#li2/android-place)


##  Google Place Autocomplete & Search in Rx

This library is a wrapper of Google Place SDK in Rx:

- Two options to search place:
    - Option 1: Launch built-in autocomplete activity
    - Option 2: Get place predictions programmatically
- Use Rx to break with the OnActivityResult and callback implementation.
- Geocoder to get address components from latLng and address full name.

<img width="300" alt="android-form-validation" src="https://github.com/li2/android-place/blob/master/place_autocomplete.gif">

Further reading https://medium.com/@li2/android-practice-google-place-autocomplete-search-in-rx-79686271d840


## Android Last Known Location (location permission and service)

This library provices
- Rx way to request location permission and service,
- Rx way to get last known location,
- Extension functions to open system location settings and App settings page.


## Usage

[MainFragment](https://github.com/li2/android-place/blob/master/app/src/main/java/me/li2/android/placesample/MainFragment.kt)


```kotlin
val placeAutoComplete = PlaceAutoComplete(context, context.getString(R.string.google_api_key))

placeAutoComplete.launchPlaceAutocompleteActivity(activity)
        .subscribeBy(onSuccess = { place: Place ->
        }, onError = {
        })

placeAutoComplete.getPlacePredictions(query)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribeBy(onNext = { predictions: List<AutocompletePrediction> ->
        }, onError = {
        })  

activity?.ifLocationAllowed(onError = {
    toast(it.message.toString())
}, onResult = { result: RequestLocationResult ->
    when (result) {
        RequestLocationResult.ALLOWED -> {
            // location permission granted and service is on,
            // it's good time to get last know location
            requestLastKnownLocation()
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

LastKnownLocationUtils.requestLastKnownLocation(context)
    .subscribeBy(onNext = { location ->
        GeocoderUtils.getAddressComponents(context, location.latitude, location.longitude)
    }, onError = { exception ->
    })
```



## Download

```gradle
implementation 'com.github.li2.android-geography:location:latest_version'
implementation 'com.github.li2.android-geography:place:latest_version'
```



## License

```
    Copyright (C) 2020 Weiyi Li

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```