package net.davegoddin.trigbag

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import net.davegoddin.trigbag.model.TrigClusterItem

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<TrigClusterItem>
) : DefaultClusterRenderer<TrigClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: TrigClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)

        val unbaggable : Array<String> = arrayOf("Destroyed", "Inaccessible", "Possibly missing")
        if (unbaggable.contains(item.trigPoint.first.condition))
        {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.no_access_marker_48))
        }
    }

}