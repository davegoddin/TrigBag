package net.davegoddin.trigbag

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.davegoddin.trigbag.viewmodel.LocationViewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var REQUEST_LOCATION_CODE = 101
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val locationViewModel: LocationViewModel by viewModels()
    lateinit var navController : NavController

    val DEFAULT_UPDATE_INTERVAL : Long  = 5
    val FAST_UPDATE_INTERVAL : Long = 5



    override fun onMapReady(googleMap: GoogleMap) {
        val sydney = LatLng(-33.852, 151.211)
        googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // location request configuration
        locationRequest = LocationRequest.create()
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // initialise location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // update viewmodel
                locationViewModel.updateLocation(locationResult.lastLocation)
            }

        }

        // check location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        //set initial location in viewmodel
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location: Location? ->
            if(location == null) {
                Toast.makeText(this, "No location found", Toast.LENGTH_SHORT).show()
            } else {
                locationViewModel.updateLocation(location)
            }
        }

        // start requesting location updates
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // call map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

        // set up bottom navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.NearFragment, R.id.SearchFragment, R.id.TrigsFragment))
        bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    private fun checkLocationAvailability() {
        if (!(getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            noGPSAlert()
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }


    private fun noGPSAlert() {
        AlertDialog.Builder(this) .setTitle("Enable Location")
            .setMessage("Please enable location settings to use app")
            .setPositiveButton("Location settings") { _, _ -> startActivity(

            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ) } .setNegativeButton("Cancel") { _, _ -> } .show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            getLocation()
        }
    }


}