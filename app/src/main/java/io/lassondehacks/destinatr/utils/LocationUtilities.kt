package io.lassondehacks.destinatr.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class LocationUtilities {

    companion object {
        fun getBoundingBoxAround(point: LatLng, radius: Float): LatLngBounds {
            val R = 6371.0  // earth radius in km

            val x1 = point.longitude - Math.toDegrees(radius / R / Math.cos(Math.toRadians(point.latitude)))

            val x2 = point.longitude + Math.toDegrees(radius / R / Math.cos(Math.toRadians(point.latitude)))

            val y1 = point.latitude + Math.toDegrees(radius / R)

            val y2 = point.latitude - Math.toDegrees(radius / R)

            return LatLngBounds(LatLng(y2, x1), LatLng(y1, x2))
        }
    }
}