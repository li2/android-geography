[![](https://jitpack.io/v/li2/android-place.svg)](https://jitpack.io/#li2/android-place)



# Android Place - Google Place Autocomplete & Search in Rx

This library is a wrapper of Google Place SDK in Rx:

- Two options to search place:
    - Option 1: Launch built-in autocomplete activity
    - Option 2: Get place predictions programmatically

- Use Rx to break with the OnActivityResult and callback implementation.

<img width="300" alt="android-form-validation" src="https://github.com/li2/android-place/blob/develop/place_autocomplete.gif">

You can read [my article on Medium](https://medium.com/@li2/android-practice-google-place-autocomplete-search-in-rx-79686271d840) for more details.



## Usage

[PlaceAutocompleteFragment](..develop/app/src/main/java/me/li2/android/placesample/PlaceAutocompleteFragment.kt)

```kotlin
val placeAutoComplete = PlaceAutoComplete(context, context.getString(R.string.google_api_key))

placeAutoComplete.launchPlaceAutocompleteActivity(activity)
        .subscribeBy(onSuccess = { place ->
        }, onError = { 
        })
```



## Download

```gradle
implementation 'com.github.li2:android-place:latest_version'
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