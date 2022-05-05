package net.davegoddin.trigbag

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import net.davegoddin.trigbag.data.AppDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.*
import net.davegoddin.trigbag.model.TrigClusterItem
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.Visit
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var visibleMarkers : MutableList<Marker>
    var currentZoom : Float? = null

    private lateinit var clusterManager : ClusterManager<TrigClusterItem>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val supportMapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(
            OnMapReadyCallback {
                onMapReady(it)
            }
        )

        val navBar : BottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar.isVisible = true

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {

        var visiblePoints : Map<TrigPoint, List<Visit>>
        currentZoom = 5.85f

        //initialise default camera position to centre on UK
        val ukCenter = LatLng(54.9, -3.15)
        val ukCamPos =
            CameraPosition.builder().target(ukCenter).zoom(currentZoom!!).bearing(0f).tilt(0f)
                .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(ukCamPos))

        val db = AppDatabase.getInstance(requireContext())
        clusterManager = ClusterManager(context, googleMap)
        clusterManager.renderer = CustomClusterRenderer(requireContext(), googleMap, clusterManager)

        fun clickListener() = ClusterManager.OnClusterItemClickListener<TrigClusterItem>
        {
            findNavController().navigate(R.id.action_SearchFragment_to_detailFragment)
            true
        }

        clusterManager.renderer.setOnClusterItemClickListener(clickListener())

        // set event listeners for camera idle
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnCameraIdleListener {

            // boundaries of visible area as lat/lon
            val neLat = googleMap.projection.visibleRegion.latLngBounds.northeast.latitude
            val neLong = googleMap.projection.visibleRegion.latLngBounds.northeast.longitude
            val swLat = googleMap.projection.visibleRegion.latLngBounds.southwest.latitude
            val swLong = googleMap.projection.visibleRegion.latLngBounds.southwest.longitude

            // query db for points within set range
            visiblePoints = runBlocking {
                db.trigPointDao().getWithinBounds(neLat, neLong, swLat, swLong)
            }

            // convert to trigclusteritem
            val trigClusterItems = visiblePoints.map {TrigClusterItem(it.toPair())}

            // clear current items and replace with only visible items - performance upgrade
            clusterManager.clearItems()
            clusterManager.cluster()
            clusterManager.addItems(trigClusterItems)

        }

        googleMap.setOnMarkerClickListener(clusterManager)

        // set location enabled
        googleMap.isMyLocationEnabled = true


        // initial set all points
        GlobalScope.launch(Dispatchers.IO) {
            val points = db.trigPointDao().getAll()
            withContext(Dispatchers.Main) {
                points.forEach {
                    clusterManager.addItem(TrigClusterItem(it.toPair()))
                }
            }
        }


    }


}