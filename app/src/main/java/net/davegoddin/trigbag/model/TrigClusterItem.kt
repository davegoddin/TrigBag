package net.davegoddin.trigbag.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class TrigClusterItem(val item: TrigPointDisplay) : ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(item.trigPoint.latitude, item.trigPoint.longitude)
    }

    override fun getTitle(): String? {
        return item.trigPoint.name
    }

    override fun getSnippet(): String? {
        return item.trigPoint.type
    }
}