/*
 * Created by Weiyi Li on 10/03/20.
 * https://github.com/li2
 */
package me.li2.android.location

enum class RequestLocationResult(val descriptioin: String){
    ALLOWED("Location permission is granted and location service is on"),
    PERMISSION_DENIED("Location permission is denied"),
    PERMISSION_DENIED_NOT_ASK_AGAIN("Location permission is denied & don't ask again"),
    SERVICE_OFF("Location service is turned off"),
}
