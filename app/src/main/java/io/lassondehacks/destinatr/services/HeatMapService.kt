package io.lassondehacks.destinatr.services

import android.provider.ContactsContract
import com.beust.klaxon.*
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.WeightedLatLng
import java.util.*

class HeatMapService {

    companion object {
        val API_URL = "http://159.203.14.223:8080"

        fun getWeightedLatLngArray(location: LatLng, radius: Int, cb: (array: List<WeightedLatLng>?) -> Unit) {
            "${API_URL}/rating/${location.longitude}/${location.latitude}/${radius}/${Date().time}".httpGet()
                    .responseString { request, response, result ->
                        result.fold({ d ->
                            val parser: com.beust.klaxon.Parser = com.beust.klaxon.Parser()
                            val stringBuilder: StringBuilder = StringBuilder(d)
                            val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                            val array = json.array<JsonObject>("ratings")
                            var list = emptyList<WeightedLatLng>()
                            if (array!!.size == 0) {
                                cb(null)
                            } else {
                                for (i in 0..array!!.size - 1) {
                                    var lng = array[i].obj("position")!!.array<Double>("coordinates")!![0]
                                    var lat = array[i].obj("position")!!.array<Double>("coordinates")!![1]
                                    var value = array[i].int("value")
                                    list = list.plus(WeightedLatLng(LatLng(lat, lng), value!!.toDouble()))
                                }
                                cb(list)
                            }
                        }, { err ->
                        })
                    }
        }
    }

}
