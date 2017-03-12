package io.lassondehacks.destinatr.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class PointClusterItem(val pos: LatLng) : ClusterItem {
    override fun getPosition(): LatLng {
        return pos
    }
}