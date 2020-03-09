package me.li2.android.location

sealed class LocationException(message: String) : Exception(message)
object LocationServiceTurnedOffException : LocationException("Location service is off")
object LocationPermissionDeniedException : LocationException("Location permission is denied")
object LastKnownLocationNotFoundException : LocationException("Last known location not found")
