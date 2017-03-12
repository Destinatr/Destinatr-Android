package io.lassondehacks.destinatr.utils

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class ParkingClusterRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<PointClusterItem>) : DefaultClusterRenderer<PointClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: PointClusterItem?, markerOptions: MarkerOptions?) {
        markerOptions!!.icon(item!!.getIcon())
        super.onBeforeClusterItemRendered(item, markerOptions)
    }
}