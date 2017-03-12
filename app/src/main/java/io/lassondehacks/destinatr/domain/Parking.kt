package io.lassondehacks.destinatr.domain

import com.google.android.gms.maps.model.LatLng

data class Parking(
        var position: LatLng,
        var restriction: Restriction?,
        var rating: Float,
        var free: Boolean?,
        var hourPrize: Float?
)