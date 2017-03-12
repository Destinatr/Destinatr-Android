package io.lassondehacks.destinatr.utils

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import io.lassondehacks.destinatr.R

class PointClusterItem(val pos: LatLng, val free: Boolean) : ClusterItem {

    fun getIcon(): BitmapDescriptor? {
        if (free)
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_free)
        else
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_paying)
    }

    override fun getPosition(): LatLng {
        return pos
    }
}