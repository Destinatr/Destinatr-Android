package io.lassondehacks.destinatr.services

import com.beust.klaxon.*
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.WeightedLatLng

class HeatMapService {

    companion object {
        val API_URL = "http://159.203.14.223:8080"

        fun getWeightedLatLngArray(location: LatLng, radius: Int, cb: (array: List<WeightedLatLng>) -> Unit) {
            cb(listOf(
                    WeightedLatLng(LatLng(45.421543, -71.963374), 5.0),
                    WeightedLatLng(LatLng(45.421543, -71.963371), 4.9),
                    WeightedLatLng(LatLng(45.421543, -71.963372), 3.8),
                    WeightedLatLng(LatLng(45.421543, -71.963365), 2.7),
                    WeightedLatLng(LatLng(45.421543, -71.963355), 1.6),
                    WeightedLatLng(LatLng(45.421543, -71.963378), 2.5),
                    WeightedLatLng(LatLng(45.421543, -71.963355), 3.4),
                    WeightedLatLng(LatLng(45.421543, -71.963395), 4.3),
                    WeightedLatLng(LatLng(45.421543, -71.963275), 5.0),
                    WeightedLatLng(LatLng(45.421543, -71.961375), 4.1),
                    WeightedLatLng(LatLng(45.421543, -71.961355), 3.0),
                    WeightedLatLng(LatLng(45.421543, -71.963736), 2.9),
                    WeightedLatLng(LatLng(45.421543, -71.963177), 1.8),
                    WeightedLatLng(LatLng(45.421543, -71.963175), 2.7),
                    WeightedLatLng(LatLng(45.421543, -71.961375), 3.6),
                    WeightedLatLng(LatLng(45.421543, -71.962375), 4.5),
                    WeightedLatLng(LatLng(45.421543, -71.964375), 5.0),
                    WeightedLatLng(LatLng(45.421543, -71.965375), 4.4),
                    WeightedLatLng(LatLng(45.421543, -71.963365), 3.3),
                    WeightedLatLng(LatLng(45.421543, -71.963779), 2.2),
                    WeightedLatLng(LatLng(45.421543, -71.969375), 1.1),
                    WeightedLatLng(LatLng(45.421543, -71.969365), 2.0),
                    WeightedLatLng(LatLng(45.421543, -71.963675), 3.2),
                    WeightedLatLng(LatLng(45.421543, -71.963371), 4.3),
                    WeightedLatLng(LatLng(45.421543, -71.963370), 5.0),
                    WeightedLatLng(LatLng(45.422543, -71.963374), 5.0),
                    WeightedLatLng(LatLng(45.423543, -71.963371), 4.9),
                    WeightedLatLng(LatLng(45.424543, -71.963372), 3.8),
                    WeightedLatLng(LatLng(45.425543, -71.963365), 2.7),
                    WeightedLatLng(LatLng(45.426543, -71.963355), 1.6),
                    WeightedLatLng(LatLng(45.427543, -71.963378), 2.5),
                    WeightedLatLng(LatLng(45.428543, -71.963355), 3.4),
                    WeightedLatLng(LatLng(45.429543, -71.963395), 4.3),
                    WeightedLatLng(LatLng(45.421443, -71.963275), 5.0),
                    WeightedLatLng(LatLng(45.421343, -71.961375), 4.1),
                    WeightedLatLng(LatLng(45.421243, -71.961355), 3.0),
                    WeightedLatLng(LatLng(45.421143, -71.963736), 2.9),
                    WeightedLatLng(LatLng(45.424543, -71.963177), 1.8),
                    WeightedLatLng(LatLng(45.423543, -71.963175), 2.7),
                    WeightedLatLng(LatLng(45.422553, -71.961375), 3.6),
                    WeightedLatLng(LatLng(45.421543, -71.962375), 4.5),
                    WeightedLatLng(LatLng(45.421533, -71.964375), 5.0),
                    WeightedLatLng(LatLng(45.421523, -71.965375), 4.4),
                    WeightedLatLng(LatLng(45.421143, -71.963365), 3.3),
                    WeightedLatLng(LatLng(45.421743, -71.963779), 2.2),
                    WeightedLatLng(LatLng(45.421843, -71.969375), 1.1),
                    WeightedLatLng(LatLng(45.421943, -71.969365), 2.0),
                    WeightedLatLng(LatLng(45.461543, -71.963675), 3.2),
                    WeightedLatLng(LatLng(45.481543, -71.963371), 4.3),
                    WeightedLatLng(LatLng(45.423543, -71.963370), 5.0)
                    ))
        }
    }

}
