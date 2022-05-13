package net.davegoddin.trigbag

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
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
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.*
import net.davegoddin.trigbag.model.*
import net.davegoddin.trigbag.service.PostcodeService
import net.davegoddin.trigbag.service.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.jar.Manifest

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
    private lateinit var searchBar : SearchView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingPermission")
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

        searchBar = view.findViewById(R.id.sch_search_searchbar)


        // check for internet connection and show/hide search bar
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // check for initial connection on view creation
        if (!hasInternetConnection(connectivityManager))
        {
            searchBar.isVisible = false
        }

        // register listener for change in connection
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            // network has become available
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                //confirm connection available and make search bar visible if so
                if (hasInternetConnection(connectivityManager))
                {
                    requireActivity().runOnUiThread(object : Runnable {
                        override fun run() {
                            searchBar.isVisible = true
                        }
                    })
                }
            }

            // network has been lost
            override fun onLost(network: Network) {
                super.onLost(network)
                // check whether any network still available e.g. if mobile lost but wifi remains
                if (!hasInternetConnection(connectivityManager))
                {
                    // hide search bar on UI thread
                    requireActivity().runOnUiThread(object : Runnable {
                        override fun run() {
                            searchBar.isVisible = false
                        }
                    })
                }


            }
        })

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

        googleMap.clear()

        // initialise searchbar
        val searchBar : SearchView = requireActivity().findViewById(R.id.sch_search_searchbar)
        searchBar.queryHint = "Search for a postcode"
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // ignore text being entered
            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

            // when search submitted
            override fun onQueryTextSubmit(query: String?): Boolean {

                //check for empty query
                if (query == null || query.trim() == "") return true

                // API call
                val service = ServiceBuilder.buildService(PostcodeService::class.java)
                val requestCall = service.getPostcode(query)

                requestCall.enqueue(object : Callback<Postcode> {
                    override fun onResponse(call: Call<Postcode>, response: Response<Postcode>) {

                        //check for successful response with matched postcode and full "unit postcode" level response to ensure lat/lon data is availabe
                        if (response.isSuccessful && response.body() != null && response.body()!!.status == "match" && response.body()!!.matchType == "unit_postcode"){
                            // move camera to location
                            val postcodeLatLng = LatLng(response.body()!!.data.latitude.toDouble(), response.body()!!.data.longitude.toDouble())
                            val postcodeCamPos =
                                CameraPosition.builder().target(postcodeLatLng).zoom(12f).bearing(0f).tilt(0f)
                                    .build()
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(postcodeCamPos))
                        }
                        else
                        {
                            // alert that postcode not found
                            Snackbar.make(requireView(), "Postcode not found", Snackbar.LENGTH_SHORT).show()
                        }

                        // hide keyboard and clear searchbar
                        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                        searchBar.setQuery("", false)

                    }

                    override fun onFailure(call: Call<Postcode>, t: Throwable) {
                        //alert that postcode not found
                        Snackbar.make(requireView(), "Postcode not found", Snackbar.LENGTH_SHORT).show()

                        // hide keyboard and clear searchbar
                        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                        searchBar.setQuery("", false)
                    }
                })
                // override default behaviour
                return true
            }
        })


        var visiblePoints : List<TrigPointDisplay>
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
            val args : Bundle = Bundle()
            args.putString("trigPointDisplay", Gson().toJson(it.item))
            findNavController().navigate(R.id.action_SearchFragment_to_detailFragment, args)
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
            val trigClusterItems = visiblePoints.map {TrigClusterItem(it)}

            // clear current items and replace with only visible items - performance upgrade
            clusterManager.clearItems()
            clusterManager.cluster()
            clusterManager.addItems(trigClusterItems)

        }

        googleMap.setOnMarkerClickListener(clusterManager)

        // set location enabled
        googleMap.isMyLocationEnabled = true


    }


    @SuppressLint("MissingPermission")
    private fun hasInternetConnection(connectivityManager : ConnectivityManager) : Boolean
    {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}