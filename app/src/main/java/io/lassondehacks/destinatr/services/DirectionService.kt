package io.lassondehacks.destinatr.services

import com.beust.klaxon.*
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.maps.model.LatLng
import io.lassondehacks.destinatr.R
import io.lassondehacks.destinatr.domain.DirectionInfo
import io.lassondehacks.destinatr.domain.Parking
import org.w3c.dom.NodeList


/**
 * Created by richerarc on 2017-03-11
 */


class DirectionService(val onDataRegister: (directions: Array<DirectionInfo>) -> Unit) {

    val directionApiBaseUrlJson = "https://maps.googleapis.com/maps/api/directions/json?"
    val directionApiBaseUrlXml = "https://maps.googleapis.com/maps/api/directions/xml?"
    val key = "AIzaSyA2VPQnLUTxLowZhRhk-bwpj-zXTtD3H3U" // make Kotlin work again

    fun getDirectionInfo(from: LatLng, to: LatLng) {
        ParkingService.getPrediction(to, 300, { err, parking ->
            if((parking!!.position.latitude != 0.0) && (parking.position.longitude != 0.0))
            {
                "${directionApiBaseUrlJson}origin=${from.latitude},${from.longitude}&destination=${parking!!.position.latitude},${parking!!.position.longitude}&units=metric&mode=driving&key=${key}".httpGet().responseString { request, response, result ->
                    result.fold({ d1 ->
                        getAllDirections(from, to, parking, d1)
                    }, { err ->

                    })
                }
            } else {
                "${directionApiBaseUrlJson}origin=${from.latitude},${from.longitude}&destination=${to.latitude},${to.longitude}&units=metric&mode=driving&key=${key}".httpGet().responseString { request, response, result ->
                    result.fold({ d1 ->
                        getAllDirections(from, to, parking, d1)
                    }, { err ->

                    })
                }
            }
        })
    }

    fun getAllDirections(from: LatLng, to: LatLng, parking: Parking, d1: String) {
        if((parking!!.position.latitude != 0.0) && (parking.position.longitude != 0.0))
        {
            "${directionApiBaseUrlJson}origin=${parking!!.position.latitude},${parking!!.position.longitude}&destination=${to.latitude},${to.longitude}&units=metric&mode=walking&key=${key}".httpGet().responseString { request, response, result ->
                result.fold({ d2 ->
                    val parser: Parser = Parser()
                    val sBd1: StringBuilder = StringBuilder(d1)
                    val jsond1: JsonObject = parser.parse(sBd1) as JsonObject
                    val sBd2: StringBuilder = StringBuilder(d2)
                    val jsond2: JsonObject = parser.parse(sBd2) as JsonObject
                    this.onDataRegister(getAllInfo(jsond1, jsond2))
                }, { err ->

                })
            }
        } else {
            val parser: Parser = Parser()
            val sBd1: StringBuilder = StringBuilder(d1)
            val json: JsonObject = parser.parse(sBd1) as JsonObject
            this.onDataRegister(getAllInfo(json, null))
        }
    }

    fun getAllInfo(jsonSTP: JsonObject, jsonPTE: JsonObject?): Array<DirectionInfo> {
        val infoStartToPark = DirectionInfo(
            getDurationText(jsonSTP),
            getDurationValue(jsonSTP),
            getDistanceText(jsonSTP),
            getDistanceValue(jsonSTP),
            getStartAddress(jsonSTP),
            getEndAddress(jsonSTP),
            getDirection(jsonSTP))

        if(jsonPTE != null) {
            val infoParkToEnd = DirectionInfo(
                    getDurationText(jsonPTE),
                    getDurationValue(jsonPTE),
                    getDistanceText(jsonPTE),
                    getDistanceValue(jsonPTE),
                    getStartAddress(jsonPTE),
                    getEndAddress(jsonPTE),
                    getDirection(jsonPTE))

            return arrayOf(infoStartToPark, infoParkToEnd)
        }

        return arrayOf(infoStartToPark)
    }

    fun getDurationText(json: JsonObject): String {

        try {
            val drText = json.array<JsonObject>("routes")!![0].array<JsonObject>("legs")!![0].obj("duration")!!.string("text")
            return drText!!
        } catch (e: Exception) {
            return "0"
        }

    }

    fun getDurationValue(json: JsonObject): Int {

        try {
            val drVal = json.array<JsonObject>("routes")!![0].array<JsonObject>("legs")!![0].obj("duration")!!.int("value")
            return drVal!!
        } catch (e: Exception) {
            return -1
        }

    }

    fun getDistanceText(json: JsonObject): String {

        try {
            val distText = json.array<JsonObject>("routes")!![0].array<JsonObject>("legs")!![0].obj("distance")!!.string("text")
            return distText!!
        } catch (e: Exception) {
            return "-1 année lumière"
        }
    }

    fun getDistanceValue(json: JsonObject): Int {
        try {
            val distVal = json.array<JsonObject>("routes")!![0].array<JsonObject>("legs")!![0].obj("distance")!!.int("value")
            return distVal!!
        } catch (e: Exception) {
            return -1
        }
    }

    fun getStartAddress(json: JsonObject): String {
        try {
            val stAddr = json.array<JsonObject>("routes")!![0].array<JsonObject>("legs")!![0].string("start_address")
            return stAddr!!
        } catch (e: Exception) {
            return "000"
        }

    }

    fun getEndAddress(json: JsonObject): String {
        try {
            val endAddr = json.array<JsonObject>("routes")!![0].array<JsonObject>("legs")!![0].string("end_address")
            return endAddr!!
        } catch (e: Exception) {
            return "-1"
        }

    }

    fun getDirection(json: JsonObject): ArrayList<LatLng> {
        val listGeopoints = ArrayList<LatLng>()
        val routes = json.array<JsonObject>("routes")!!
        if(routes.size != 0) {
            val legs = routes[0].array<JsonObject>("legs")!!
            val steps = legs[0].array<JsonObject>("steps")
            if (steps!!.count() > 0) {
                for (i in 0..steps!!.count() - 1) {
                    val obj1 = steps!![i]

                    val startLoc = obj1.obj("start_location")

                    listGeopoints.add(LatLng(startLoc!!.double("lat")!!, startLoc!!.double("lng")!!))

                    val polyline = obj1.obj("polyline")

                    val arr = decodePoly(polyline!!.string("points")!!)
                    for (j in arr.indices) {
                        listGeopoints.add(LatLng(arr[j].latitude, arr[j].longitude))
                    }

                    val endLoc = obj1.obj("end_location")

                    listGeopoints.add(LatLng(endLoc!!.double("lat")!!, endLoc!!.double("lng")!!))
                }
            }
        }

        return listGeopoints
    }

    private fun getNodeIndex(nl: NodeList, nodename: String): Int {
        for (i in 0..nl.getLength() - 1) {
            if (nl.item(i).getNodeName() == nodename)
                return i
        }
        return -1
    }

    private fun decodePoly(encoded: String): ArrayList<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val position = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(position)
        }
        return poly
    }

    val MODE_DRIVING = "driving"
    val MODE_WALKING = "walking"
}