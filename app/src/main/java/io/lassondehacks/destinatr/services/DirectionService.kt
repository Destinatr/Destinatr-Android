package io.lassondehacks.destinatr.services

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.android.gms.maps.model.LatLng
import io.lassondehacks.destinatr.R
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Created by richerarc on 2017-03-11
 */

class DirectionService {

    companion object {

        val directionApiBaseUrl = "https://maps.googleapis.com/maps/api/directions/json?"
        val key = "AIzaSyA2VPQnLUTxLowZhRhk-bwpj-zXTtD3H3U" // make Kotlin work again

        fun getDocument(from: LatLng, to: LatLng): Document? {

            "${directionApiBaseUrl}origin=${from.latitude},${from.longitude}&destination=${to.latitude},${to.longitude}&key=${key}".httpGet().responseString { request, response, result ->
                //do something with response
                result.fold({ data ->

                    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    val doc = builder.parse(data)
                    val durationText = getDurationText(doc)
                    val durationVal = getDurationValue(doc)
                    val distanceText = getDistanceText(doc)
                    val distanceVal = getDistanceValue(doc)
                    val fromAddr = getStartAddress(doc)
                    val toAddr = getEndAddress(doc)
                    val copyright = getCopyRights(doc)
                    val directions = getDirection(doc)

                }, { err ->
                    println("error")
                })
            }

            return null
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

        fun get (from: LatLng, to: LatLng){

            getDocument(from, to)

        }
    }
}


class GMapV2Direction {



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
        /*
         * while (en.hasMoreElements()) { type type = (type) en.nextElement();
         *
         * }
         */

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

        /*
         * NodeList nl1; if(doc.getElementsByTagName("distance")!=null){ nl1=
         * doc.getElementsByTagName("distance");
         *
         * Node node1 = nl1.item(nl1.getLength() - 1); NodeList nl2 = null; if
         * (node1.getChildNodes() != null) { nl2 = node1.getChildNodes(); Node
         * node2 = nl2.item(getNodeIndex(nl2, "value")); Log.d("DistanceText",
         * node2.getTextContent()); return node2.getTextContent(); } else return
         * "-1";} else return "-1";
         */
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

        /*
         * NodeList nl1 = doc.getElementsByTagName("distance"); Node node1 =
         * null; if (nl1.getLength() > 0) node1 = nl1.item(nl1.getLength() - 1);
         * if (node1 != null) { NodeList nl2 = node1.getChildNodes(); Node node2
         * = nl2.item(getNodeIndex(nl2, "value")); Log.i("DistanceValue",
         * node2.getTextContent()); return
         * Integer.parseInt(node2.getTextContent()); } else return 0;
         */
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

    companion object {
        val MODE_DRIVING = "driving"
        val MODE_WALKING = "walking"
    }
}