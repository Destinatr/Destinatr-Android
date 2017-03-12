package io.lassondehacks.destinatr.services

import com.beust.klaxon.*
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.maps.model.LatLng
import io.lassondehacks.destinatr.domain.Parking
import io.lassondehacks.destinatr.domain.Restriction
import org.xml.sax.Parser

class ParkingService {

    companion object {
        val API_URL = "http://159.203.14.223:8080"

        fun getParkingsAtLocationAtPage(location: LatLng, radius: Int, page: Int, freeFilter: Boolean = true, payingFilter: Boolean = true): Triple<String?, List<Parking>?, Int?> {
            var (request, response, result) = "$API_URL/parking/near/${location.longitude}/${location.latitude}/$radius/$page/100"
                    .httpGet().timeout(60000).timeoutRead(60000).responseString()
            result.fold({ d ->
                val parser: com.beust.klaxon.Parser = com.beust.klaxon.Parser()
                val stringBuilder: StringBuilder = StringBuilder(d)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                var parkings = getParkingList(json.array<JsonObject>("parkings")!!, freeFilter, payingFilter)
                return Triple(null, parkings, json.int("remainingPages"))
            }, { err ->
                return Triple(err.message, null, null)
            })

        }

        fun getParkingList(array: JsonArray<JsonObject>, freeFilter: Boolean = true, payingFilter: Boolean = true): List<Parking> {
            var arrayList = arrayListOf<Parking>()
            for (obj in array) {
                var position = LatLng(
                        obj.obj("position")!!.array<Double>("coordinates")!![1],
                        obj.obj("position")!!.array<Double>("coordinates")!![0]
                )
                var rating = obj.int("rating")
                var restrictionObj = obj.obj("restriction")
                var restrictions: Restriction? = null
                if (restrictionObj != null) {
                    restrictions = Restriction(
                            restrictionObj!!.string("code")!!,
                            restrictionObj.boolean("toujours"),
                            restrictionObj.array<String>("journee")!!.toTypedArray(),
                            restrictionObj.array<String>("moisDebut")!!.toTypedArray(),
                            restrictionObj.array<String>("moisFin")!!.toTypedArray(),
                            restrictionObj.array<String>("heureDebut")!!.toTypedArray(),
                            restrictionObj.array<String>("heureFin")!!.toTypedArray()
                    )
                }
                var free = obj.boolean("free")
                var parking = Parking(position, restrictions, rating?.toFloat() ?: 0.0f, free, null)
                if(!freeFilter && parking.free!! || !payingFilter && !parking.free!!)
                    continue
                arrayList.add(parking)
            }
            return arrayList
        }

        fun getPrediction(location: LatLng, radius: Int, cb: (err: String?, Parking?) -> Unit) {
            "$API_URL/parking/nearest/${location.longitude}/${location.latitude}/$radius/"
                    .httpGet().timeout(60000).timeoutRead(60000)
                    .responseString { request, response, result ->
                        result.fold({ d ->
                            val parser: com.beust.klaxon.Parser = com.beust.klaxon.Parser()
                            val stringBuilder: StringBuilder = StringBuilder(d)
                            val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                            val parkingJson = json.obj("parkings")
                            if(parkingJson != null) {
                                val latitude = parkingJson!!.obj("position")!!.array<Double>("coordinates")!![1]
                                val longitude = parkingJson.obj("position")!!.array<Double>("coordinates")!![0]
                                val rating = parkingJson.int("rating")
                                var free = parkingJson.boolean("free")
                                cb(null, Parking(LatLng(latitude, longitude), null, rating?.toFloat() ?: 0.0f, free, null))
                            } else {
                                cb(null, Parking(LatLng(0.0, 0.0), null, 0f, false, null))
                            }
                        }, { err ->
                            cb(err.message, null)
                        })
                    }
        }
    }
}