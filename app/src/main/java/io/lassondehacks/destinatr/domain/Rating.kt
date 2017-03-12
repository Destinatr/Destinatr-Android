package io.lassondehacks.destinatr.domain

import com.google.android.gms.maps.model.LatLng
import java.security.Timestamp

data class Rating(
        var position: LatLng,
        var timestamp: Long,
        var value: Float
)