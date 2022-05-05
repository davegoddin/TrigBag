package net.davegoddin.trigbag

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import net.davegoddin.trigbag.data.AppDatabase
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.Visit
import net.davegoddin.trigbag.viewmodel.LocationViewModel
import java.util.*

class NearFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // register observer for current location
        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer {
            getNearbyPoints(it)
        })

        val navBar : BottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar.isVisible = true

        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_near, container, false)
    }


    private fun getNearbyPoints(location: Location) : SortedMap<TrigPoint, List<Visit>>?
    {
        val db = AppDatabase.getInstance(requireContext())
        var points : Map<TrigPoint, List<Visit>>
        val initialRange : Double = 7500.0

        if (location !=null)
        {
            val currentLatLng = LatLng(location.latitude, location.longitude)
            val neLatLong = SphericalUtil.computeOffset(currentLatLng, initialRange, 45.0)
            val swLatLong = SphericalUtil.computeOffset(currentLatLng, initialRange,225.0)

            // query db for points within set range
            points = runBlocking {
                db.trigPointDao().getWithinBounds(neLatLong.latitude, neLatLong.longitude, swLatLong.latitude, swLatLong.longitude)
            }

            // sort list by distance to current location
            var sortedPoints = points.toSortedMap(compareBy<TrigPoint> { SphericalUtil.computeDistanceBetween(currentLatLng, LatLng(it.latitude, it.longitude)) })

            sortedPoints.forEach {
                Log.d("Points", "${it.key.name}: ${SphericalUtil.computeDistanceBetween(currentLatLng, LatLng(it.key.latitude, it.key.longitude))}")
            }

            return sortedPoints
        }

        //fallback, return null
        return null

    }


}