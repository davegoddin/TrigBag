package net.davegoddin.trigbag

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import net.davegoddin.trigbag.data.AppDatabase
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*
import net.davegoddin.trigbag.adapter.TrigListItemAdapter
import net.davegoddin.trigbag.model.TrigPointDisplay
import net.davegoddin.trigbag.viewmodel.LocationViewModel

class NearFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private var nearbyPoints = mutableListOf<TrigPointDisplay>()
    private lateinit var adapter : TrigListItemAdapter

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


    private fun getNearbyPoints(location: Location)
    {
        val db = AppDatabase.getInstance(requireContext())
        var points : List<TrigPointDisplay>
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
            var sortedPoints = points.sortedBy{ SphericalUtil.computeDistanceBetween(currentLatLng, LatLng(it.trigPoint.latitude, it.trigPoint.longitude)) }

            sortedPoints.forEach {
                it.distance = SphericalUtil.computeDistanceBetween(currentLatLng, LatLng(it.trigPoint.latitude, it.trigPoint.longitude))
            }

            nearbyPoints.clear()
            nearbyPoints.addAll(sortedPoints)
            adapter.notifyDataSetChanged()

        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerNear)
        adapter = TrigListItemAdapter(this, nearbyPoints)
        recyclerView.adapter = adapter

    }

//    override fun onResume() {
//        super.onResume()
//        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        (requireActivity() as AppCompatActivity).supportActionBar?.show()
//    }


}