package io.lassondehacks.destinatr.domain

import com.google.android.gms.maps.model.LatLng

/**
 * Created by richerarc on 2017-03-11.
 */

data class DirectionInfo (
        var durationText: String = "",
        var durationVal: Int = 0,
        var distanceText: String = "",
        var distanceVal: Int = 0,
        var fromAddr: String = "",
        var toAddr: String = "",
        var directions: ArrayList<LatLng>? = null)