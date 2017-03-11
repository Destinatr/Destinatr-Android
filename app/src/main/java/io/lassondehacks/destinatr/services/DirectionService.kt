package io.lassondehacks.destinatr.services

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.android.gms.maps.model.LatLng
import io.lassondehacks.destinatr.R
import io.lassondehacks.destinatr.domain.DirectionInfo
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Created by richerarc on 2017-03-11
 */



class DirectionService(val onDataRegister: (directions: DirectionInfo) -> Unit) {

        val directionApiBaseUrlJson = "https://maps.googleapis.com/maps/api/directions/json?"
        val directionApiBaseUrlXml = "https://maps.googleapis.com/maps/api/directions/xml?"
        val key = "AIzaSyA2VPQnLUTxLowZhRhk-bwpj-zXTtD3H3U" // make Kotlin work again

        fun getDirectionInfo(from: LatLng, to: LatLng) {

            "${directionApiBaseUrlXml}origin=${from.latitude},${from.longitude}&destination=${to.latitude},${to.longitude}&key=${key}".httpGet().responseString { request, response, result ->
                result.fold({ d ->
                    this.onDataRegister(getAllInfo(d))
                }, { err ->

                })

            }
        }

        fun getAllInfo(xml: String): DirectionInfo {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

            val doc = builder.parse(InputSource(ByteArrayInputStream(xml.toByteArray(charset("utf-8")))))

            var info = DirectionInfo()
            info.durationText = getDurationText(doc)
            info.durationVal = getDurationValue(doc)
            info.distanceText = getDistanceText(doc)
            info.distanceVal = getDistanceValue(doc)
            info.fromAddr = getStartAddress(doc)
            info.toAddr = getEndAddress(doc)
            info.copyright = getCopyRights(doc)
            info.directions = getDirection(doc)

            return info
        }

        fun getDurationText(doc: Document): String {
            try {

                val nl1 = doc.getElementsByTagName("duration")
                val node1 = nl1.item(0)
                val nl2 = node1.getChildNodes()
                val node2 = nl2.item(getNodeIndex(nl2, "text"))
                return node2.getTextContent()
            } catch (e: Exception) {
                return "0"
            }

        }

        fun getDurationValue(doc: Document): Int {
            try {
                val nl1 = doc.getElementsByTagName("duration")
                val node1 = nl1.item(0)
                val nl2 = node1.getChildNodes()
                val node2 = nl2.item(getNodeIndex(nl2, "value"))
                return Integer.parseInt(node2.getTextContent())
            } catch (e: Exception) {
                return -1
            }

        }

        fun getDistanceText(doc: Document): String {
            try {
                val nl1: NodeList
                nl1 = doc.getElementsByTagName("distance")

                val node1 = nl1.item(nl1.getLength() - 1)
                var nl2: NodeList? = null
                nl2 = node1.getChildNodes()
                val node2 = nl2!!.item(getNodeIndex(nl2, "value"))
                return node2.getTextContent()
            } catch (e: Exception) {
                return "-1"
            }
        }

        fun getDistanceValue(doc: Document): Int {
            try {
                val nl1 = doc.getElementsByTagName("distance")
                var node1: Node? = null
                node1 = nl1.item(nl1.getLength() - 1)
                val nl2 = node1!!.getChildNodes()
                val node2 = nl2.item(getNodeIndex(nl2, "value"))
                return Integer.parseInt(node2.getTextContent())
            } catch (e: Exception) {
                return -1
            }
        }

        fun getStartAddress(doc: Document): String {
            try {
                val nl1 = doc.getElementsByTagName("start_address")
                val node1 = nl1.item(0)
                return node1.getTextContent()
            } catch (e: Exception) {
                return "-1"
            }

        }

        fun getEndAddress(doc: Document): String {
            try {
                val nl1 = doc.getElementsByTagName("end_address")
                val node1 = nl1.item(0)
                return node1.getTextContent()
            } catch (e: Exception) {
                return "-1"
            }

        }

        fun getCopyRights(doc: Document): String {
            try {
                val nl1 = doc.getElementsByTagName("copyrights")
                val node1 = nl1.item(0)
                return node1.getTextContent()
            } catch (e: Exception) {
                return "-1"
            }

        }

        fun getDirection(doc: Document): ArrayList<LatLng> {
            val nl1: NodeList
            var nl2: NodeList
            var nl3: NodeList
            val listGeopoints = ArrayList<LatLng>()
            nl1 = doc.getElementsByTagName("step")
            if (nl1.getLength() > 0) {
                for (i in 0..nl1.getLength() - 1) {
                    val node1 = nl1.item(i)
                    nl2 = node1.getChildNodes()

                    var locationNode = nl2
                            .item(getNodeIndex(nl2, "start_location"))
                    nl3 = locationNode.getChildNodes()
                    var latNode = nl3.item(getNodeIndex(nl3, "lat"))
                    var lat = java.lang.Double.parseDouble(latNode.getTextContent())
                    var lngNode = nl3.item(getNodeIndex(nl3, "lng"))
                    var lng = java.lang.Double.parseDouble(lngNode.getTextContent())
                    listGeopoints.add(LatLng(lat, lng))

                    locationNode = nl2.item(getNodeIndex(nl2, "polyline"))
                    nl3 = locationNode.getChildNodes()
                    latNode = nl3.item(getNodeIndex(nl3, "points"))
                    val arr = decodePoly(latNode.getTextContent())
                    for (j in arr.indices) {
                        listGeopoints.add(LatLng(arr[j].latitude, arr[j].longitude))
                    }

                    locationNode = nl2.item(getNodeIndex(nl2, "end_location"))
                    nl3 = locationNode.getChildNodes()
                    latNode = nl3.item(getNodeIndex(nl3, "lat"))
                    lat = java.lang.Double.parseDouble(latNode.getTextContent())
                    lngNode = nl3.item(getNodeIndex(nl3, "lng"))
                    lng = java.lang.Double.parseDouble(lngNode.getTextContent())
                    listGeopoints.add(LatLng(lat, lng))
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