package net.davegoddin.trigbag.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class TrigClusterItem(val trigPoint: Pair<TrigPoint, List<Visit>>) : ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(trigPoint.first.latitude, trigPoint.first.longitude)
    }

    override fun getTitle(): String? {
        return trigPoint.first.name
    }

    override fun getSnippet(): String? {
        return trigPoint.first.type
    }
}